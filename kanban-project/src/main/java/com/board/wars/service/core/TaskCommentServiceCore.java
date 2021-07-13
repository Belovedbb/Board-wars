package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.TaskComment;
import com.board.wars.domain.Task;
import com.board.wars.mapper.TaskCommentMapper;
import com.board.wars.payload.request.TaskCommentRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskCommentResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.TaskCommentRepository;
import com.board.wars.repository.TaskRepository;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.service.TaskCommentService;
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
public class TaskCommentServiceCore implements TaskCommentService {
    final private TaskCommentMapper mapper;
    final private TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;

    private final HistoryRepository historyRepository;
    private final KafkaTemplate<String, Object> kanbanTemplate;

    public TaskCommentServiceCore(TaskCommentMapper mapper, TaskCommentRepository taskCommentRepository,
                                  TaskRepository taskRepository, HistoryRepository historyRepository, KafkaTemplate<String, Object> kanbanTemplate) {
        this.mapper = mapper;
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.kanbanTemplate = kanbanTemplate;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<TaskCommentResponsePayload>> getTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId, ServerWebExchange exchange) {
        return getTaskName(projectCode, columnName, taskId)
                .flatMap(taskName -> taskCommentRepository.findByProjectColumnTask(projectCode, columnName, taskName, taskCommentId, Pageable.unpaged()))
                .filter(taskComment -> taskComment.getTaskComment() != null)
                .flatMap(taskComment ->  Mono.just(taskComment.getTaskComment()))
                .flatMap(taskComment -> mapTaskCommentToTaskCommentResponse(taskComment, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<TaskCommentResponsePayload>> getTaskComments(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange) {
        return getTaskName(projectCode, columnName, taskId)
                .flatMap(taskName -> taskCommentRepository.findByProject(projectCode,columnName, taskName, Pageable.unpaged()))
                .filter(taskComment -> !CollectionUtils.isEmpty(taskComment.getTaskComments()))
                .flatMap(taskComment ->  Mono.just(taskComment.getTaskComments()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(column -> mapTaskCommentToTaskCommentResponse(column.getT2(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<TaskCommentResponsePayload>> createTaskComment(Long projectCode, String columnName, Long taskId, TaskCommentRequestPayload taskCommentPayload,
                                                                               ServerWebExchange exchange, Authentication authentication) {
        return Mono.just(taskCommentPayload)
                .map(mapper::mapRequestPayloadToTaskCommentDomain)
                .flatMap(taskComment -> getTaskName(projectCode, columnName, taskId).map(name -> Tuples.of(name, taskComment)))
                .flatMap(taskComment -> generateTaskCommentCodeId().map(generatedCode -> Tuples.of(taskComment.getT1(), GeneralUtil.setAndGet(taskComment.getT2(), comment -> comment.setCode(generatedCode)))))
                .flatMap(payload -> taskCommentRepository.insertNewTaskComment(projectCode, columnName, payload.getT1(),  payload.getT2()))
                .filter(taskComment -> !CollectionUtils.isEmpty(taskComment.getTaskComments()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "comment does not exist", "comment").build(HttpStatus.BAD_REQUEST, null)))
                .map(BaseMultiInterface.BaseTaskComment::getTaskComments)
                .flatMapIterable(Function.identity())
                .last()
                .doOnSuccess(taskComment -> this.createAndSendLog(taskComment, authentication.getName(), Utilities.PersistentLog.CREATE, null))
                .filter(taskComment -> taskComment.getComment().equals(taskCommentPayload.getComment()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to verify update", "comment").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(taskComment -> mapTaskCommentToTaskCommentResponse(taskComment, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<TaskCommentResponsePayload>> updateTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId,
                                                                               TaskCommentRequestPayload taskCommentPayload, ServerWebExchange exchange, Authentication authentication) {
        Mono<String> taskNameStream = getTaskName(projectCode, columnName, taskId);
        return taskNameStream.flatMap(taskName -> taskCommentRepository.findByProjectColumnTask(projectCode, columnName, taskName,  taskCommentId, Pageable.unpaged())
                .map(taskComment -> mapper.updateTaskCommentFromRequestPayload(taskComment.getTaskComment(), taskCommentPayload))
                .flatMap(payload -> taskNameStream.flatMap(task -> taskCommentRepository.updateTaskComment(projectCode, columnName, task, payload) ))
                .filter(taskComment -> taskComment.getTaskComment() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to update", "comment").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(taskComment ->  Mono.just(taskComment.getTaskComment()))
                .doOnSuccess(taskComment -> this.createAndSendLog(taskComment, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .flatMap(data -> mapTaskCommentToTaskCommentResponse(data, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null)));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<Void>> deleteTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId, ServerWebExchange exchange, Authentication authentication) {
        Mono<String> taskNameStream = getTaskName(projectCode, columnName, taskId);
        return taskNameStream.flatMap(taskName -> taskCommentRepository.findByProjectColumnTask(projectCode, columnName, taskName, taskCommentId,  Pageable.unpaged()))
                .filter(taskComment -> taskComment.getTaskComment() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "task comment does not exist", "comment").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(taskComment ->  Mono.just(taskComment.getTaskComment()))
                .doOnSuccess(taskComment -> this.createAndSendLog(taskComment, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(task -> taskNameStream.flatMap(taskName -> taskCommentRepository.deleteTaskComment(projectCode, columnName, taskName, taskCommentId)))
                .then()
                .then(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    private Mono<TaskCommentResponsePayload> mapTaskCommentToTaskCommentResponse(TaskComment taskComment, ServerWebExchange exchange) {
        TaskCommentResponsePayload payload = mapper.mapTaskCommentDomainToResponsePayload(taskComment);
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

    private Mono<Long> generateTaskCommentCodeId() {
        Long generatedCode = GeneralUtil.randomCodeGenerator(0, 234567892L);
        return taskCommentRepository.taskCommentCodeIdExist(generatedCode).filter(c -> c).flatMap(codeExist -> generateTaskCommentCodeId()).defaultIfEmpty(generatedCode);
    }

    private History logTaskCommentCreate(TaskComment taskComment, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("TaskComment ").append(taskComment.getCode()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New TaskComment", LocalDateTime.now(), "TaskComment", username, taskComment);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskCommentUpdate(TaskComment taskComment, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("TaskComment ").append(taskComment.getCode()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update TaskComment", LocalDateTime.now(), "TaskComment", username, taskComment);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskCommentDelete(TaskComment taskComment, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("TaskComment ").append(taskComment.getCode()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete TaskComment", LocalDateTime.now(), "TaskComment", username, taskComment);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskCommentOperation(TaskComment taskComment, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "TaskComment " + operation, LocalDateTime.now(), "TaskComment", username, taskComment);
        history.setCategory(HistoryCategory.MILD);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(TaskComment taskComment, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(taskComment, username, logType, operation);
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

    private History getHistoryLog(TaskComment taskComment, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logTaskCommentUpdate(taskComment, username);
            }
            case CREATE: {
                return logTaskCommentCreate(taskComment, username);
            }
            case DELETE: {
                return  logTaskCommentDelete(taskComment, username);
            }
            case LOG: {
                return  logTaskCommentOperation(taskComment, username, operation);
            }
        }
        return null;
    }
}
