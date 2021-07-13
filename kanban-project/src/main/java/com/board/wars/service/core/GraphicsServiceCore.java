package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.domain.*;
import com.board.wars.mapper.GraphicsMapper;
import com.board.wars.payload.response.ActivityFrequencyDataResponsePayload;
import com.board.wars.payload.response.CumulativeFlowDiagramResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.repository.ActivityFrequencyRepository;
import com.board.wars.repository.CumulativeFlowDiagramRepository;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.ProjectRepository;
import com.board.wars.service.GraphicsService;
import com.board.wars.util.Utilities;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class GraphicsServiceCore implements GraphicsService {

    private final CumulativeFlowDiagramRepository cumulativeFlowDiagramRepository;
    private final ProjectRepository projectRepository;
    private final GraphicsMapper mapper;

    private final HistoryRepository historyRepository;
    private final KafkaTemplate<String, Object> kanbanTemplate;
    final private  ActivityFrequencyRepository activityFrequencyRepository;

    public GraphicsServiceCore(CumulativeFlowDiagramRepository cumulativeFlowDiagramRepository, ProjectRepository projectRepository,
                               GraphicsMapper mapper, HistoryRepository historyRepository, KafkaTemplate<String, Object> kanbanTemplate,
                               ActivityFrequencyRepository activityFrequencyRepository) {
        this.cumulativeFlowDiagramRepository = cumulativeFlowDiagramRepository;
        this.projectRepository = projectRepository;
        this.mapper = mapper;
        this.historyRepository = historyRepository;
        this.kanbanTemplate = kanbanTemplate;
        this.activityFrequencyRepository = activityFrequencyRepository;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFDForAllDate(Long projectCode, Integer page, Integer pageSize, ServerWebExchange exchange) {
        return  cumulativeFlowDiagramRepository.findByFlowDiagram(projectCode, Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("id"))))
                .flatMap(cfd -> this.mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(cfd, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFDForDateRange(Long projectCode, Integer page, Integer size, LocalDate startDate, LocalDate endDate, ServerWebExchange exchange) {
        return  cumulativeFlowDiagramRepository.findByFlowDiagram(projectCode, startDate, endDate,
                Utilities.Pager.resolvePagerFromSize(page, size, Sort.by(Sort.Order.asc("id"))))
                .flatMap(cfd -> this.mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(cfd, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<CumulativeFlowDiagramResponsePayload>> getAllCFD(Integer page, Integer pageSize, ServerWebExchange exchange) {
        return  cumulativeFlowDiagramRepository.findAll(Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("id"))))
                .flatMap(cfd -> this.mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(cfd, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFD(Long projectCode, ServerWebExchange exchange) {
        return cumulativeFlowDiagramRepository.findByProjectCode(projectCode)
                .flatMap(cfd -> this.mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(cfd, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> createCFD(Long projectCode, CumulativeFlowDiagram payload) {
        return Mono.just(payload)
                .map(obj -> GeneralUtil.setAndGet(obj , o -> o.setProjectCode(projectCode)))
                .flatMap(cumulativeFlowDiagramRepository::save)
                .doOnSuccess(cumulativeFlowDiagram -> this.createAndSendLog(cumulativeFlowDiagram, "SYSTEM", Utilities.PersistentLog.CREATE, null))
                .flatMap(project -> this.mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(project, null))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<ActivityFrequencyDataResponsePayload>> getActivityFrequency(String year, ServerWebExchange exchange) {
        return Flux.fromIterable(this.generateTitlePoints(year))
                .flatMap(title -> activityFrequencyRepository.findByTitle(title).defaultIfEmpty(new ActivityFrequencyData(title, null)))
                .map(this::populateMissingData)
                .sort(Comparator.comparingInt(a -> Integer.parseInt(a.getTitle())))
                .flatMap(data -> mapActivityFrequencyDataToActivityFrequencyDataResponse(data, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<Long> getActivityFrequencyChartTaskDataPoint(String year, ServerWebExchange exchange) {
        return Flux.fromIterable(this.generateTitlePoints(year))
                .flatMap(title -> activityFrequencyRepository.findByTitle(title).defaultIfEmpty(new ActivityFrequencyData(title, null)))
                .map(this::populateMissingData)
                .sort(Comparator.comparingInt(a -> Integer.parseInt(a.getTitle())))
                .flatMap(data -> Flux.fromIterable(data.getMonths().stream().map(ActivityFrequencyDataPoint::getTaskCount).collect(Collectors.toList())));
    }

    private Mono<CumulativeFlowDiagramResponsePayload> mapCumulativeFlowDiagramToCumulativeFlowDiagramResponse(CumulativeFlowDiagram cumulativeFlowDiagram, ServerWebExchange exchange) {
        return Mono.just(mapper.mapCumulativeFlowDiagramDomainToResponsePayload(cumulativeFlowDiagram));
    }

    private Mono<ActivityFrequencyDataResponsePayload> mapActivityFrequencyDataToActivityFrequencyDataResponse(ActivityFrequencyData activityFrequencyData, ServerWebExchange exchange) {
        return Mono.just(mapper.mapActivityFrequencyDataDomainToResponsePayload(activityFrequencyData));
    }

    private ActivityFrequencyData populateMissingData(ActivityFrequencyData origin) {
        int from = 0;
        if(origin == null) {
            origin = new ActivityFrequencyData("", new ArrayList<>());
        }
        else if(CollectionUtils.isEmpty(origin.getMonths()) || !StringUtils.hasText(origin.getTitle())) {
            if (CollectionUtils.isEmpty(origin.getMonths())) {
                origin.setMonths(new ArrayList<>());
            } else {
                origin.setTitle("");
            }
        }else {
            from = origin.getMonths().size() - 1;
        }
        for(; from < 12; from++){
            String monthText = Month.of(from + 1).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
            origin.getMonths().add(new ActivityFrequencyDataPoint(monthText, 0L, 0L));
        }
        return origin;
    }

    //    @Scheduled(cron = "0 * * * * *")
    @Scheduled(cron = "0 0 0 * * *")
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public void updateAllActiveProjectsForCumulativeFlowDiagram() {
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalDate today = LocalDate.now();
        if(LocalDateTime.of(today, midnight).equals(LocalDateTime.now())) {
            this.projectRepository.findByStatus(Status.ACTIVE, Pageable.unpaged())
                    .filter(project -> !CollectionUtils.isEmpty(project.getColumns()) )
                    .flatMapSequential(this::registerFlowDiagrams)
                    .subscribeOn(Schedulers.parallel()).log().subscribe();
        }
    }

    @Scheduled(cron = "0 15 10 28-31 * ?")
    //@Scheduled(cron = "0 * * * * *")
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public void updateAllActivityFrequency() {
        LocalDate date = LocalDate.now();
        LocalDate last = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        if (date.getDayOfMonth() == last.getDayOfMonth()) {
            projectRepository.findByStatus(Status.ACTIVE, Pageable.unpaged())
                    .collectList().map(projects -> Tuples.of((long)projects.size(), projects.stream().map(this::getTaskCount).reduce(0L, Long::sum)))
                    .defaultIfEmpty(Tuples.of(0L, 0L))
                    .flatMap(points -> buildData(points.getT1(), points.getT2()))
                    .flatMap(activityFrequencyRepository::save)
                    .doOnSuccess(activityFrequencyData -> this.createAndSendLog(activityFrequencyData, "SYSTEM", Utilities.PersistentLog.CREATE, null))
                    .subscribe();
        }
    }

    private Mono<ActivityFrequencyData> buildData (Long projectSize, Long taskSize) {
        final String currentTitle = String.valueOf(Year.now().getValue());
        return activityFrequencyRepository.findByTitle(currentTitle)
                .flatMap(data -> activityFrequencyRepository.insertDataPoint(data, generateActivityFrequencyDataPoint(projectSize, taskSize)))
                .defaultIfEmpty(createActivityFrequencyData(currentTitle, projectSize, taskSize));
    }

    private ActivityFrequencyData createActivityFrequencyData (String title, Long projectSize, Long taskSize) {
        ActivityFrequencyData data = new ActivityFrequencyData();
        data.setTitle(title);
        data.setMonths(Collections.singletonList(this.generateActivityFrequencyDataPoint(projectSize, taskSize)));
        return data;
    }

    private ActivityFrequencyDataPoint generateActivityFrequencyDataPoint (Long projectSize, Long taskSize) {
        String monthText = Month.of(LocalDateTime.now().getMonthValue()).getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
        return new ActivityFrequencyDataPoint(monthText, projectSize, taskSize);
    }

    private Long getTaskCount(Project project) {
        long count = 0L;
        List<Column> columnList = project.getColumns();
        if(CollectionUtils.isEmpty(columnList)) return count;
        for (Column column: columnList) {
            if(column != null && !CollectionUtils.isEmpty(column.getTasks())) {
                count += column.getTasks().size();
            }
        }
        return count;
    }

    private Flux<CumulativeFlowDiagram>  registerFlowDiagrams(Project project) {
        return ensureProjectGen(project)
                .flatMapMany(proj -> Flux.fromIterable(proj.getColumns()).map(column -> createFlowDiagram(column, LocalDate.now())) )
                .flatMap(flow -> cumulativeFlowDiagramRepository.insertNewFlowDiagramToCumulativeFlowDiagram(project.getCode(), flow));
    }

    private Mono<Project> ensureProjectGen(Project project) {
        return cumulativeFlowDiagramRepository.projectCodeExist(project.getCode())
                .filter(c -> !c).flatMap(codeExist -> this.createCFD(project.getCode(), new CumulativeFlowDiagram()))
                .map(p -> project).defaultIfEmpty(project);
    }

    private List<String> generateTitlePoints(String spreadPoint) {
        Long spreadStart = Long.valueOf(spreadPoint);
        return List.of(String.valueOf(spreadStart - 1), String.valueOf(spreadStart), String.valueOf(spreadStart + 1));
    }

   private  FlowDiagram createFlowDiagram(Column column, LocalDate date) {
        FlowDiagram f = new FlowDiagram();
        f.setColumn(column);
        f.setCurrentTime(date);
        return f;
    }

    private History logCFDCommentCreate(CumulativeFlowDiagram cumulativeFlowDiagram, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("CumulativeFlowDiagram ").append(cumulativeFlowDiagram.getId())
                .append(" was successfully created for project code ").append(cumulativeFlowDiagram.getProjectCode())
                .append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New CumulativeFlowDiagram", LocalDateTime.now(), "CumulativeFlowDiagram", username, cumulativeFlowDiagram);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private History logActivityFrequency(ActivityFrequencyData activityFrequencyData, String username) {
        int size = activityFrequencyData.getMonths().size();
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("ActivityFrequencyData ").append(activityFrequencyData.getTitle())
                .append(" was successfully created  for the month of  ").append(activityFrequencyData.getMonths().get(size - 1).getMonth())
                .append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New ActivityFrequencyData",
                LocalDateTime.now(), "ActivityFrequencyData", username, activityFrequencyData.getMonths().get(size - 1));
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        return history;
    }

    private void createAndSendLog(ActivityFrequencyData activityFrequencyData, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryActivityFrequencyLog(activityFrequencyData, username, logType, operation);
        if(history != null) {
            streamHistory(history);
            persistHistory(history);
        }
    }


    private History getHistoryActivityFrequencyLog(ActivityFrequencyData activityFrequencyData, String username,
                                                   Utilities.PersistentLog logType, String operation) {
        if (logType == Utilities.PersistentLog.CREATE) {
            return logActivityFrequency(activityFrequencyData, username);
        }
        return null;
    }

    private void createAndSendLog(CumulativeFlowDiagram cumulativeFlowDiagram, String username, Utilities.PersistentLog logType, String operation) {
        History history = getHistoryLog(cumulativeFlowDiagram, username, logType, operation);
        if(history != null) {
            streamHistory(history);
            persistHistory(history);
        }
    }

    private void streamHistory(History history) {
        this.kanbanTemplate.send("kanban",  history.getId(), history);
    }

    private void persistHistory(History history) {
        historyRepository.save(new HistoryEntity("graphics", history)).subscribe();
    }

    private History getHistoryLog(CumulativeFlowDiagram cumulativeFlowDiagram, String username, Utilities.PersistentLog logType, String operation) {
        if (logType == Utilities.PersistentLog.CREATE) {
            return logCFDCommentCreate(cumulativeFlowDiagram, username);
        }
        return null;
    }

}
