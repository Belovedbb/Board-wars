package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.SubTask;
import com.board.wars.domain.Task;
import com.board.wars.mapper.SubTaskMapper;
import com.board.wars.payload.request.SubTaskRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.SubTaskResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.SubTaskRepository;
import com.board.wars.repository.TaskRepository;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.service.SubTaskService;
import com.board.wars.util.Utilities;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class SubTaskServiceCore implements SubTaskService {
    final private SubTaskMapper mapper;
    final private SubTaskRepository subTaskRepository;
    private final TaskRepository taskRepository;

    private final HistoryRepository historyRepository;
    private final KafkaTemplate<String, Object> kanbanTemplate;

    public SubTaskServiceCore(SubTaskMapper mapper, SubTaskRepository subTaskRepository, TaskRepository taskRepository,
                              HistoryRepository historyRepository, KafkaTemplate<String, Object> kanbanTemplate) {
        this.mapper = mapper;
        this.subTaskRepository = subTaskRepository;
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.kanbanTemplate = kanbanTemplate;
    }
    
    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<SubTaskResponsePayload>> getSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId, ServerWebExchange exchange) {
        return getTaskName(projectCode, columnName, taskId)
                .flatMap(taskName -> subTaskRepository.findByProjectColumnTask(projectCode, columnName, taskName, subTaskId, Pageable.unpaged()))
                .filter(subTask -> subTask.getSubTask() != null)
                .flatMap(subTask ->  Mono.just(subTask.getSubTask()))
                .flatMap(subTask -> mapSubTaskToSubTaskResponse(subTask, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<SubTaskResponsePayload>> getSubTasks(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange) {
        return getTaskName(projectCode, columnName, taskId)
                .flatMap(taskName -> subTaskRepository.findByProject(projectCode,columnName, taskName, Pageable.unpaged()))
                .filter(subTask -> !CollectionUtils.isEmpty(subTask.getSubTasks()))
                .flatMap(subTask ->  Mono.just(subTask.getSubTasks()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(column -> mapSubTaskToSubTaskResponse(column.getT2(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<SubTaskResponsePayload>> createSubTask(Long projectCode, String columnName, Long taskId, SubTaskRequestPayload subTaskPayload, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just(subTaskPayload)
                .map(mapper::mapRequestPayloadToSubTaskDomain)
                .flatMap(subTask -> getTaskName(projectCode, columnName, taskId).map(name -> Tuples.of(name, subTask)))
                .flatMap(subTask -> generateSubTaskCodeId().map(generatedCode -> Tuples.of(subTask.getT1(), GeneralUtil.setAndGet(subTask.getT2(), subTask1 -> subTask1.setCode(generatedCode)))))
                .flatMap(payload -> subTaskRepository.insertNewSubTask(projectCode, columnName, payload.getT1(),  payload.getT2()))
                .filter(subTask -> !CollectionUtils.isEmpty(subTask.getSubTasks()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "subtask does not exist", "subtask").build(HttpStatus.BAD_REQUEST, null)))
                .map(BaseMultiInterface.BaseSubTask::getSubTasks)
                .flatMapIterable(Function.identity())
                .last()
                .doOnSuccess(subTask -> this.createAndSendLog(subTask, authentication.getName(), Utilities.PersistentLog.CREATE, null))
                .filter(subTask -> subTask.getName().equals(subTaskPayload.getName()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to verify update", "subTask").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(subTask -> mapSubTaskToSubTaskResponse(subTask, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    //TODO define annotation for key modifying operators
    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<SubTaskResponsePayload>> updateSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId,
                                                                       SubTaskRequestPayload subTaskPayload, ServerWebExchange exchange, Authentication authentication) {
        Mono<String> taskNameStream = getTaskName(projectCode, columnName, taskId);
        return taskNameStream.flatMap(taskName -> subTaskRepository.findByProjectColumnTask(projectCode, columnName, taskName,  subTaskId, Pageable.unpaged())
                .map(subTask -> mapper.updateSubTaskFromRequestPayload(subTask.getSubTask(), subTaskPayload))
                .flatMap(payload -> taskNameStream.flatMap(task -> subTaskRepository.updateSubTask(projectCode, columnName, task, payload) ))
                .filter(subTask -> subTask.getSubTask() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to update", "subTask").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(subTask ->  Mono.just(subTask.getSubTask()))
                .doOnSuccess(subTask -> this.createAndSendLog(subTask, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .flatMap(data -> mapSubTaskToSubTaskResponse(data, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null)));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<Void>> deleteSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId, ServerWebExchange exchange, Authentication authentication) {
        Mono<String> taskNameStream = getTaskName(projectCode, columnName, taskId);
        return taskNameStream.flatMap(taskName -> subTaskRepository.findByProjectColumnTask(projectCode, columnName, taskName, subTaskId,  Pageable.unpaged()))
                .filter(subTask -> subTask.getSubTask() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "subtask does not exist", "subtask").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(subTask ->  Mono.just(subTask.getSubTask()))
                .doOnSuccess(subTask -> this.createAndSendLog(subTask, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(task -> taskNameStream.flatMap(taskName -> subTaskRepository.deleteSubTask(projectCode, columnName, taskName, subTaskId)))
                .then()
                .then(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    private Mono<SubTaskResponsePayload> mapSubTaskToSubTaskResponse(SubTask subTask, ServerWebExchange exchange) {
        SubTaskResponsePayload payload = mapper.mapSubTaskDomainToResponsePayload(subTask);
        return Mono.just(payload);
    }

    private Mono<String> getTaskName(Long projectCode, String columnName, Long taskId) {
        return taskRepository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()) && task.getTasks().size() >= taskId + 1)
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .elementAt(Math.toIntExact(taskId))
                .map(Task::getName);

    }

    private Mono<Long> generateSubTaskCodeId() {
        Long generatedCode = GeneralUtil.randomCodeGenerator(0, 234567892L);
        return subTaskRepository.subTaskCodeIdExist(generatedCode).filter(c -> c).flatMap(codeExist -> generateSubTaskCodeId()).defaultIfEmpty(generatedCode);
    }

    private History logSubTaskCreate(SubTask subTask, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("SubTask ").append(subTask.getName()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New SubTask", LocalDateTime.now(), "SubTask", username, subTask);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logSubTaskUpdate(SubTask subTask, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("SubTask ").append(subTask.getName()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update SubTask", LocalDateTime.now(), "SubTask", username, subTask);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logSubTaskDelete(SubTask subTask, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("SubTask ").append(subTask.getName()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete SubTask", LocalDateTime.now(), "SubTask", username, subTask);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logSubTaskOperation(SubTask subTask, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "SubTask " + operation, LocalDateTime.now(), "SubTask", username, subTask);
        history.setCategory(HistoryCategory.MILD);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(SubTask subTask, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(subTask, username, logType, operation);
        if(history != null) {
            streamHistory(history);
            persistHistory(history);
        }
    }

    private void streamHistory(History history) {
        this.kanbanTemplate.send("kanban", 0, history.getId(), history);
    }

    private void persistHistory(History history) {
        historyRepository.save(new HistoryEntity("domain", history)).subscribe();
    }

    private History getHistoryLog(SubTask subTask, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logSubTaskUpdate(subTask, username);
            }
            case CREATE: {
                return logSubTaskCreate(subTask, username);
            }
            case DELETE: {
                return  logSubTaskDelete(subTask, username);
            }
            case LOG: {
                return  logSubTaskOperation(subTask, username, operation);
            }
        }
        return null;
    }

}
