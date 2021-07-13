package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.assembler.SubTaskModelAssembler;
import com.board.wars.assembler.TaskCommentModelAssembler;
import com.board.wars.domain.*;
import com.board.wars.mapper.TaskMapper;
import com.board.wars.payload.request.TaskRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.TaskRepository;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.board.wars.service.TaskService;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class TaskServiceCore implements TaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;
    private final SubTaskModelAssembler subTaskModelAssembler;
    private final TaskCommentModelAssembler taskCommentModelAssembler;

    @Autowired
    HistoryRepository historyRepository;
    @Autowired
    KafkaTemplate<String, Object> kanbanTemplate;

    public TaskServiceCore(TaskRepository repository, TaskMapper mapper, SubTaskModelAssembler subTaskModelAssembler, TaskCommentModelAssembler taskCommentModelAssembler) {
        this.repository = repository;
        this.mapper = mapper;
        this.subTaskModelAssembler = subTaskModelAssembler;
        this.taskCommentModelAssembler = taskCommentModelAssembler;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<TaskResponsePayload>> getTask(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange) {
        Assert.notNull(taskId, "task id cannot be empty");
        return repository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()) && task.getTasks().size() >= taskId + 1)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "task does not exist", "taskId").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(data -> mapTaskToTaskResponse(projectCode, columnName, data.getT2(), data.getT1(), exchange))
                .elementAt(Math.toIntExact(taskId))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<TaskResponsePayload>> getTasks(Long projectCode, String columnName, ServerWebExchange exchange) {
        return repository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()))
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(data -> mapTaskToTaskResponse(projectCode, columnName, data.getT2(), data.getT1(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<TaskResponsePayload>> createTask(Long projectCode, String columnName, TaskRequestPayload taskPayload, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just(taskPayload)
                .map(mapper::mapRequestPayloadToTaskDomain)
                .map(task -> GeneralUtil.setAndGet(task , ts -> ts.setCreatedDate(LocalDateTime.now())))
                .map(task -> GeneralUtil.setAndGet(task , ts -> ts.setUpdatedDate(LocalDateTime.now())))
                .flatMap(task -> injectReporter(task, authentication))
                .flatMap(payload -> repository.insertNewTaskToProject(projectCode, columnName, payload))
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "task does not exist", "task").build(HttpStatus.BAD_REQUEST, null)))
                .map(BaseMultiInterface.BaseTask::getTasks)
                .flatMapIterable(Function.identity())
                .index().last()
                .doOnSuccess(task -> this.createAndSendLog(task.getT2(), authentication.getName(), Utilities.PersistentLog.CREATE, null))
                .filter(taskTuple -> taskTuple.getT2().getName().equals(taskPayload.getName()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to verify update", "task").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(data -> mapTaskToTaskResponse(projectCode, columnName, data.getT2(), data.getT1(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<TaskResponsePayload>> updateTask(Long projectCode, String columnName, Long taskId, TaskRequestPayload taskPayload, ServerWebExchange exchange, Authentication authentication) {
        return repository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()) && task.getTasks().size() >= taskId + 1)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "task does not exist", "taskId").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .elementAt(Math.toIntExact(taskId))
                .map(task -> mapper.updateTaskFromRequestPayload(task, taskPayload))
                .doOnSuccess(task -> this.createAndSendLog(task, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .map(task -> GeneralUtil.setAndGet(task , ts -> ts.setUpdatedDate(LocalDateTime.now())))
                .flatMap(payload -> repository.updateProjectTask(projectCode, columnName, payload))
                .filter(task -> task.getTask() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to update", "task").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(task ->  Mono.just(task.getTask()))
                .flatMap(data -> mapTaskToTaskResponse(projectCode, columnName, data, taskId, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<Void>> deleteTask(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange, Authentication authentication) {
        final long id = taskId + 1;
        return repository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()) && task.getTasks().size() >= id)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "task does not exist", "taskId").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .take(id).next()
                .doOnSuccess(task -> this.createAndSendLog(task, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(task -> repository.deleteProjectTask(projectCode, columnName, task.getName()))
                .then()
                .then(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<TaskResponsePayload>> moveTaskBetweenColumns(Long projectCode, String columnName, Long taskId, String transferColumnName,
                                                                             Long transferTaskId, ServerWebExchange exchange, Authentication authentication) {
        return getTaskName(projectCode, columnName, taskId)
                .flatMap(taskName -> repository.findByProjectColumnTaskName(projectCode, columnName, taskName, Pageable.unpaged()))
                .filter(task -> task.getTask() != null)
                .map(BaseSingleInterface.BaseTask::getTask)
                .doOnSuccess(task -> repository.deleteProjectTask(projectCode, columnName, task.getName()).subscribe())
                .flatMap(task -> repository.findAllTaskFromProject(projectCode, transferColumnName, Pageable.unpaged()).map(tasks -> Tuples.of(task, tasks)))
                .map(taskTuple -> setTaskMoved(taskTuple, Math.toIntExact(transferTaskId)) )
                .flatMap(tasks -> repository.updateProjectColumnTasks(projectCode,  transferColumnName, tasks.toArray(Task[]::new)))
                .doOnSuccess(task -> this.createAndSendLog(null, authentication.getName(), Utilities.PersistentLog.LOG,
                        "moved  task from column " + columnName + " of position " + taskId + " to column " + transferColumnName + " of  position " + transferTaskId))
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(task -> mapTaskToTaskResponse(projectCode, transferColumnName, task.getT2(), task.getT1(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    @Override
    public Mono<GenericResponse<Void>> getTaskState(Long projectCode, Long id) {
        return null;
    }

    private Mono<TaskResponsePayload> mapTaskToTaskResponse(final Long projectCode, final String column, final Task task, final Long pos, ServerWebExchange exchange) {
        TaskResponsePayload payload = mapper.mapTaskDomainToResponsePayload(task);
        return Mono.just(payload)
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.COLUMN_NAME_KEY, column)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.TASK_ID_KEY, pos)))
                .filter(taskResponse -> !CollectionUtils.isEmpty(task.getSubTasks()))
                .flatMap(response -> this.insertTaskSubTasks(response, task, exchange))
                .defaultIfEmpty(payload)
                .filter(taskResponse -> !CollectionUtils.isEmpty(task.getComments()))
                .flatMap(response -> this.insertTaskComments(response, task, exchange))
                .defaultIfEmpty(payload).map(load -> GeneralUtil.setAndGet(load, response -> response.setPosition(pos)));
    }

    private Mono<TaskResponsePayload> insertTaskSubTasks(TaskResponsePayload response, Task domain, ServerWebExchange exchange) {
        return subTaskModelAssembler.toSubCollectionModel(Flux.fromIterable(domain.getSubTasks()), exchange)
                .map(taskResponse -> GeneralUtil.setAndGet(response, taskResponsePayload -> taskResponsePayload.setSubTasks(taskResponse)));
    }

    private Mono<TaskResponsePayload> insertTaskComments(TaskResponsePayload response, Task domain, ServerWebExchange exchange) {
        return taskCommentModelAssembler.toSubCollectionModel(Flux.fromIterable(domain.getComments()), exchange)
                .map(taskCommentResponse -> GeneralUtil.setAndGet(response, taskResponsePayload -> taskResponsePayload.setComments(taskCommentResponse)));
    }

    private Mono<String> getTaskName(Long projectCode, String columnName, Long taskId) {
        return repository.findAllTaskFromProject(projectCode, columnName, Pageable.unpaged())
                .filter(task -> !CollectionUtils.isEmpty(task.getTasks()) && task.getTasks().size() >= taskId + 1)
                .flatMap(task ->  Mono.just(task.getTasks()))
                .flatMapIterable(Function.identity())
                .elementAt(Math.toIntExact(taskId))
                .map(Task::getName);

    }

    private List<Task> setTaskMoved(Tuple2<Task, BaseMultiInterface.BaseTask<Task>> taskTuple, int pos) {
        Task element = taskTuple.getT1();
        List<Task> tasks = taskTuple.getT2().getTasks();
        if(CollectionUtils.isEmpty(tasks)) {
            tasks = new ArrayList<>();
            return GeneralUtil.setAndGet(tasks, t -> t.add(element));
        }else if(pos >= tasks.size()){
            return GeneralUtil.setAndGet(tasks, t -> t.add(element));
        }else {
            return GeneralUtil.setAndGet(tasks, t -> t.add(pos, element));
        }
    }

    private Mono<Task> injectReporter(Task task, Authentication auth) {
        TeamUser teamUser = new TeamUser();
        teamUser.setType(TeamUser.MemberType.USER);
        teamUser.setId(auth.getName());
        Member member = new Member();
        member.setTeamUser(teamUser);
        return Mono.just(task).map( taskElement -> GeneralUtil.setAndGet(taskElement, ts -> ts.setReporter(member)));
    }

    private History logTaskCreate(Task task, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Task ").append(task.getName()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New Task", LocalDateTime.now(), "Task", username, task);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskUpdate(Task task, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Task ").append(task.getName()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update Task", LocalDateTime.now(), "Task", username, task);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskDelete(Task task, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Task ").append(task.getName()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete Task", LocalDateTime.now(), "Task", username, task);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logTaskOperation(Task task, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(" Successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "Task Operation" , LocalDateTime.now(), "Task", username, task);
        history.setCategory(HistoryCategory.MILD);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(Task task, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(task, username, logType, operation);
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

    private History getHistoryLog(Task task, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logTaskUpdate(task, username);
            }
            case CREATE: {
                return logTaskCreate(task, username);
            }
            case DELETE: {
                return  logTaskDelete(task, username);
            }
            case LOG: {
                return  logTaskOperation(task, username, operation);
            }
        }
        return null;
    }
}
