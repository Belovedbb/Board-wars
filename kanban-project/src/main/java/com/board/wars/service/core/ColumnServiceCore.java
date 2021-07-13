package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.assembler.TaskModelAssembler;
import com.board.wars.domain.Column;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.Project;
import com.board.wars.mapper.ColumnMapper;
import com.board.wars.payload.request.ColumnRequestPayload;
import com.board.wars.payload.response.ColumnResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.repository.ColumnRepository;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.service.ColumnService;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class ColumnServiceCore implements ColumnService {

    final private ColumnMapper mapper;
    final private ColumnRepository columnRepository;
    final private TaskModelAssembler taskModelAssembler;

    final private HistoryRepository historyRepository;
    final private KafkaTemplate<String, Object> kanbanTemplate;

    public ColumnServiceCore(ColumnMapper mapper, ColumnRepository columnRepository, TaskModelAssembler taskModelAssembler, HistoryRepository historyRepository, KafkaTemplate<String, Object> kanbanTemplate) {
        this.mapper = mapper;
        this.columnRepository = columnRepository;
        this.taskModelAssembler = taskModelAssembler;
        this.historyRepository = historyRepository;
        this.kanbanTemplate = kanbanTemplate;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<ColumnResponsePayload>> getColumnProject(Long projectCode, String name, ServerWebExchange exchange) {
        return columnRepository.findByProjectCodeAndColumnName(projectCode, name, Pageable.unpaged())
                .filter(column -> column.getColumn() != null)
                .flatMap(column ->  Mono.just(column.getColumn()))
                .flatMap(column -> mapColumnToColumnResponse(projectCode, column, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Flux<GenericResponse<ColumnResponsePayload>> getColumnsFromProject(Long projectCode, ServerWebExchange exchange) {
        return columnRepository.findByProjectCode(projectCode, Pageable.unpaged())
                .filter(column -> !CollectionUtils.isEmpty(column.getColumns()))
                .flatMap(column ->  Mono.just(column.getColumns()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(column -> mapColumnToColumnResponse(projectCode, column.getT2(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<ColumnResponsePayload>> createColumnForProject(Long projectCode, ColumnRequestPayload columnPayload, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just(columnPayload)
                .map(mapper::mapRequestPayloadToColumnDomain)
                .flatMap(payload -> columnRepository.insertNewColumnToProject(projectCode, payload))
                .filter(column -> !CollectionUtils.isEmpty(column.getColumns()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "project does not exist", "column").build(HttpStatus.BAD_REQUEST, null)))
                .map(BaseMultiInterface.BaseColumn::getColumns)
                .flatMapIterable(Function.identity())
                .last()
                .doOnSuccess(column -> this.createAndSendLog(column, authentication.getName(), Utilities.PersistentLog.CREATE, null))
                .filter(column -> column.getName().equals(columnPayload.getName()))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to verify update", "column").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(column -> mapColumnToColumnResponse(projectCode, column, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<ColumnResponsePayload>> updateColumnForProject(Long projectCode, String name, ColumnRequestPayload columnPayload, ServerWebExchange exchange, Authentication authentication) {
        return columnRepository.findByProjectCodeAndColumnName(projectCode, name, Pageable.unpaged())
                .map(column -> mapper.updateColumnFromRequestPayload(column.getColumn(), columnPayload))
                .flatMap(payload -> columnRepository.updateProjectColumn(projectCode, payload))
                .filter(column -> column.getColumn() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.BAD_REQUEST.value(),
                        "unable to update", "column").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(column ->  Mono.just(column.getColumn()))
                .doOnSuccess(column -> this.createAndSendLog(column, authentication.getName(), Utilities.PersistentLog.UPDATE, null))
                .flatMap(data -> mapColumnToColumnResponse(projectCode, data, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<Void>> deleteColumnForProject(Long projectId, String name, ServerWebExchange exchange, Authentication authentication) {
        return columnRepository.findByProjectCodeAndColumnName(projectId, name, Pageable.unpaged())
                .filter(column -> column.getColumn() != null)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "column does not exist", "column").build(HttpStatus.NOT_FOUND, null)))
                .flatMap(column ->  Mono.just(column.getColumn()))
                .doOnSuccess(column -> this.createAndSendLog(column, authentication.getName(), Utilities.PersistentLog.DELETE, null))
                .flatMap(task -> columnRepository.deleteProjectColumn(projectId, name))
                .then()
                .then(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Flux<GenericResponse<ColumnResponsePayload>> batchCreateColumnForProject(Long projectCode, ColumnRequestPayload[] columnPayload, ServerWebExchange exchange, Authentication authentication) {
        return Flux.fromArray(columnPayload).flatMap(payload -> this.createColumnForProject(projectCode, payload, exchange, authentication));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Flux<GenericResponse<ColumnResponsePayload>> swapColumn(Long projectCode, String firstColumnName, String secondColumnName, ServerWebExchange exchange, Authentication authentication) {
        return columnRepository.findByProjectCode(projectCode, Pageable.unpaged())
                .filter(column -> hasColumnParameter(column.getColumns(), firstColumnName, secondColumnName))
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "column(s) not found", "columnName").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(column ->  Mono.just(swapColumns(column.getColumns(), firstColumnName, secondColumnName)))
                .flatMap(swappedColumns -> columnRepository.updateProjectColumns(projectCode, swappedColumns.toArray(Column[]::new)))
                .doOnSuccess(column -> this.createAndSendLog(null, authentication.getName(), Utilities.PersistentLog.LOG, "swapped from column " + firstColumnName + " to " + secondColumnName))
                .flatMap(column ->  Mono.just(column.getColumns()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(column -> mapColumnToColumnResponse(projectCode, column.getT2(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    //zero index based pos
    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Flux<GenericResponse<ColumnResponsePayload>> moveColumn(Long projectCode, String name, int position, ServerWebExchange exchange, Authentication authentication) {
        return columnRepository.findByProjectCode(projectCode, Pageable.unpaged())
                .filter(column -> !CollectionUtils.isEmpty(column.getColumns()) && column.getColumns().stream().filter(col -> col.getName().equals(name)).count() == 1)
                .filter(column -> column.getColumns().size() > position && position > -1)
                .switchIfEmpty(Mono.error(new Utilities.KanbanErrorBuilder().addErrorStatus((long)HttpStatus.NOT_FOUND.value(),
                        "column(s) not found", "columnName").build(HttpStatus.BAD_REQUEST, null)))
                .flatMap(column ->  moveColumn(column.getColumns(), name, position))
                .flatMap(swappedColumns -> columnRepository.updateProjectColumns(projectCode, swappedColumns.toArray(Column[]::new)))
                .doOnSuccess(column -> this.createAndSendLog(null, authentication.getName(), Utilities.PersistentLog.LOG, "moved  column " + name + " to position " + position))
                .flatMap(column ->  Mono.just(column.getColumns()))
                .flatMapIterable(Function.identity())
                .index()
                .flatMap(column -> mapColumnToColumnResponse(projectCode, column.getT2(), exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.BAD_REQUEST));
    }

    private Mono<ColumnResponsePayload> mapColumnToColumnResponse(Long projectCode, Column column, ServerWebExchange exchange) {
        ColumnResponsePayload payload = mapper.mapColumnDomainToResponsePayload(column);
        return Mono.just(payload)
                .filter(columnResponse -> !CollectionUtils.isEmpty(column.getTasks()))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.COLUMN_NAME_KEY, column.getName())))
                .flatMap(response -> this.insertColumnTasks(response, column, exchange))
                .defaultIfEmpty(payload);
    }

    private Mono<ColumnResponsePayload> insertColumnTasks(ColumnResponsePayload response, Column domain, ServerWebExchange exchange) {
        return taskModelAssembler.toSubCollectionModel(Flux.fromIterable(domain.getTasks()), exchange)
                .map(taskResponse -> GeneralUtil.setAndGet(response, columnResponsePayload -> columnResponsePayload.setTasks(taskResponse)));
    }

    private boolean hasColumnParameter(List<Column> columns, String firstColumnName, String secondColumnName) {
        List<Integer> indexes = getColumnParameter(columns, firstColumnName, secondColumnName);
        return !CollectionUtils.isEmpty(indexes) && indexes.size() == 2;
    }


    private List<Integer> getColumnParameter(List<Column> columns, String firstColumnName, String secondColumnName) {
        if (CollectionUtils.isEmpty(columns) || columns.size() < 2) return Collections.emptyList();
        AtomicInteger position = new AtomicInteger(-1);
        return columns.stream()
                .peek(x -> position.incrementAndGet())
                .filter(column -> firstColumnName.equals(column.getName())  || secondColumnName.equals(column.getName()))
                .map(column -> position.get()).collect(Collectors.toList());
    }

    //TODO improve swapping in one pass
    private List<Column> swapColumns(List<Column> columns, String firstColumnName, String secondColumnName) {
        List<Column> uniqueColumns = new ArrayList<>(columns);
        List<Integer> indexes = getColumnParameter(columns, firstColumnName, secondColumnName);
        Collections.swap(uniqueColumns, indexes.get(0), indexes.get(1));
        return uniqueColumns;
    }

    private Mono<ArrayList<Column>> moveColumn(List<Column> columns, String name, int position) {
        return Flux.fromIterable(columns)
                .collectList()
                .flatMap(columnList -> getColumnsWithIndex(columnList, name))
                .map(columnsWithIndex -> moveColumnToPos(columnsWithIndex, position));
    }

    private Mono<Tuple2<Long, List<Column>>> getColumnsWithIndex(List<Column> columnList, final String name) {
        return Flux.fromIterable(columnList).index().filter(column -> name.equals(column.getT2().getName())).next().map(tuple -> Tuples.of(tuple.getT1(), columnList));
    }

    private ArrayList<Column> moveColumnToPos(Tuple2<Long, List<Column>> columnsWithIndex, int position) {
        List<Column> columnList = columnsWithIndex.getT2();
        moveElement(columnList, Math.toIntExact(columnsWithIndex.getT1()), position);
        return new ArrayList<>(columnList);
    }

    private void moveElement(List<?> list, int a, int b) {
        if(a == b) return;
        int direction = a > b ? 1 : -1;
        int minor = Math.min(a, b);
        int major = Math.max(b, a);
        Collections.rotate(list.subList(minor, major + 1), direction);
    }

    private History logColumnCreate(Column column, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Column ").append(column.getName()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New Column", LocalDateTime.now(), "Column", username, column);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logColumnUpdate(Column column, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Column ").append(column.getName()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update Column", LocalDateTime.now(), "Column", username, column);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logColumnDelete(Column column, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Column ").append(column.getName()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete Column", LocalDateTime.now(), "Column", username, column);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logColumnOperation(Column column, String username, String operation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Successfully ").append(operation);
        messageBuilder.append(" Operation performed By ").append(username);//moved from operaton 1 to pos 2
        History history = new History(messageBuilder.toString(), "Column " + operation, LocalDateTime.now(), "Column", username, column);
        history.setCategory(HistoryCategory.MILD);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(Column column, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(column, username, logType, operation);
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

    private History getHistoryLog(Column column, String username, Utilities.PersistentLog logType, String operation) {
        switch (logType) {
            case UPDATE: {
                return logColumnUpdate(column, username);
            }
            case CREATE: {
                return logColumnCreate(column, username);
            }
            case DELETE: {
                return  logColumnDelete(column, username);
            }
            case LOG: {
                return  logColumnOperation(column, username, operation);
            }
        }
        return null;
    }

}
