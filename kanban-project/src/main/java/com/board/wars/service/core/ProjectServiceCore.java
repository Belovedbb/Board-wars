package com.board.wars.service.core;

import com.board.wars.GeneralUtil;
import com.board.wars.History;
import com.board.wars.HistoryCategory;
import com.board.wars.assembler.ColumnModelAssembler;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.domain.Project;
import com.board.wars.domain.Status;
import com.board.wars.mapper.ProjectMapper;
import com.board.wars.payload.request.ProjectRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.ProjectResponsePayload;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.repository.ProjectRepository;
import com.board.wars.service.ProjectService;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class ProjectServiceCore implements ProjectService {

    final private ProjectMapper mapper;
    private final ProjectRepository projectRepository;
    private final KafkaTemplate<String, Object> kanbanTemplate;

    private final ColumnModelAssembler columnAssembler;
    @Autowired
    HistoryRepository historyRepository;

    public ProjectServiceCore(ProjectMapper mapper, ProjectRepository projectRepository, ColumnModelAssembler columnAssembler,
                              KafkaTemplate<String, Object> template) {
        this.mapper = mapper;
        this.projectRepository = projectRepository;
        this.columnAssembler = columnAssembler;
        this.kanbanTemplate = template;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Mono<GenericResponse<ProjectResponsePayload>> getProject(Long code, ServerWebExchange exchange) {
        return projectRepository.findByCode(code)
                .flatMap(project -> this.mapProjectToProjectResponse(project, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<GenericResponse<ProjectResponsePayload>> getProjects(Boolean active, Integer page, Integer pageSize, ServerWebExchange exchange) {
        Pageable pageable = Utilities.Pager.resolvePagerFromSize(page, pageSize, Sort.by(Sort.Order.asc("id")));
        return  (active == null ? projectRepository.findAll(pageable) :
                projectRepository.findByStatus(Status.ACTIVE, pageable))
                .flatMap(project -> this.mapProjectToProjectResponse(project, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, HttpStatus.NOT_FOUND));
    }
//TODO check given roles for creating
    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<ProjectResponsePayload>> createProject(ProjectRequestPayload projectRequest, ServerWebExchange exchange, Authentication authentication) {
        return Mono.just(projectRequest)
                .map(mapper::mapRequestPayloadToProjectDomain)
                .flatMap(this::injectCreateProperty)
                .flatMap(projectRepository::save)
                .doOnSuccess(project -> this.createAndSendLog(project, authentication.getName(), Utilities.PersistentLog.CREATE))
                .flatMap(project -> this.mapProjectToProjectResponse(project, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<ProjectResponsePayload>> updateProject(Long code, ProjectRequestPayload projectPayload, ServerWebExchange exchange, Authentication authentication) {
        return projectRepository.findByCode(code)
                .map(project -> mapper.updateProjectFromRequestPayload(project, projectPayload))
                .flatMap(projectRepository::save)
                .doOnSuccess(project -> this.createAndSendLog(project, authentication.getName(), Utilities.PersistentLog.UPDATE))
                .flatMap(project -> this.mapProjectToProjectResponse(project, exchange))
                .map(GenericResponse::new)
                .switchIfEmpty(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(true, null));
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @Retry(name = KANBAN)
    public Mono<GenericResponse<Void>> deleteProject(Long code, ServerWebExchange exchange, Authentication authentication) {
        return projectRepository.findByCode(code)
                .doOnSuccess(project -> this.createAndSendLog(project, authentication.getName(), Utilities.PersistentLog.DELETE))
                .flatMap(projectRepository::delete)
                .then(Utilities.KanbanErrorBuilder.emptyResponseWithThrow(false, null));
    }

    private Mono<ProjectResponsePayload> mapProjectToProjectResponse(Project project, ServerWebExchange exchange) {
        Mono<ProjectResponsePayload> payload = Mono.just(mapper.mapProjectDomainToResponsePayload(project));
        return payload.filter(projectResponse -> !CollectionUtils.isEmpty(project.getColumns()))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, project.getCode())))
                //.map(response -> res)
                .flatMap(response -> this.insertProjectColumns(response, project, exchange))
                .switchIfEmpty(payload);
    }

    private Mono<ProjectResponsePayload> insertProjectColumns(ProjectResponsePayload response, Project domain, ServerWebExchange exchange) {
        return columnAssembler.toSubCollectionModel(Flux.fromIterable(domain.getColumns()), exchange)
                .map(columnResponse -> GeneralUtil.setAndGet(response, projectResponsePayload -> projectResponsePayload.setColumns(columnResponse)));
    }

     private Mono<Project> injectCreateProperty(Project project) {
        return generateProjectCodeId().map(code -> GeneralUtil.setAndGet(project, project1 -> project1.setCode(code)))
         .map(project1 -> GeneralUtil.setAndGet(project1, prop -> prop.setStartPeriod(LocalDateTime.now())));
    }

    private Mono<Long> generateProjectCodeId() {
        Long generatedCode = GeneralUtil.randomCodeGenerator(0, 234567892L);
        return projectRepository.projectCodeIdExist(generatedCode).filter(c -> c).flatMap(codeExist -> generateProjectCodeId()).defaultIfEmpty(generatedCode);
    }

    private History logProjectCreate(Project project, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Project ").append(project.getName()).append(" with code ").append(project.getCode()).append(" was successfully created.");
        messageBuilder.append(" Created By ").append(username);
        History history = new History(messageBuilder.toString(), "New Project", LocalDateTime.now(), "Project", username, project);
        history.setCategory(HistoryCategory.SUCCESS);
        history.setId(String.valueOf(UUID.randomUUID()));
        history.setData(project);
        return history;
    }

    private History logProjectUpdate(Project project, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Project ").append(project.getName()).append(" with code ").append(project.getCode()).append(" was successfully updated.");
        messageBuilder.append(" Updated By ").append(username);
        History history = new History(messageBuilder.toString(), "Update Project", LocalDateTime.now(), "Project", username, project);
        history.setCategory(HistoryCategory.WARNING);
        history.setId(String.valueOf(UUID.randomUUID()));
        history.setData(project);
        return history;
    }

    private History logProjectDelete(Project project, String username) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Project ").append(project.getName()).append(" with code ").append(project.getCode()).append(" was successfully deleted.");
        messageBuilder.append(" Deleted By ").append(username);
        History history = new History(messageBuilder.toString(), "Delete Project", LocalDateTime.now(), "Project", username, project);
        history.setCategory(HistoryCategory.DANGER);
        history.setId(String.valueOf(UUID.randomUUID()));
        history.setData(project);
        return history;
    }

    private void createAndSendLog(Project project, String username, Utilities.PersistentLog logType) {
        History history = getHistoryLog(project, username, logType);
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

    private History getHistoryLog(Project project, String username, Utilities.PersistentLog logType) {
        switch (logType) {
            case UPDATE: {
                return logProjectUpdate(project, username);
            }
            case CREATE: {
                return logProjectCreate(project, username);
            }
            case DELETE: {
                return  logProjectDelete(project, username);
            }
        }
        return null;
    }
}
