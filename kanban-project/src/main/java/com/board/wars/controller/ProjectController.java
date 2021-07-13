package com.board.wars.controller;

import com.board.wars.assembler.ProjectModelAssembler;
import com.board.wars.payload.request.ProjectRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.ProjectResponsePayload;
import com.board.wars.service.ProjectService;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/{auth}/kanban/project")
public class ProjectController {

    final private ProjectService projectService;
    private final ProjectModelAssembler assembler;

    public ProjectController(ProjectService projectService, ProjectModelAssembler assembler) {
        this.projectService = projectService;
        this.assembler = assembler;
    }

    @PostMapping
    public Mono<GenericResponse<ProjectResponsePayload>> createProject(@PathVariable String auth, @RequestBody ProjectRequestPayload createProjectPayload,
                                                                       ServerWebExchange exchange, Authentication authentication){
        return projectService.createProject(createProjectPayload, exchange, authentication)
                 .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+resp.getBody().getCode()));
    }

    @PatchMapping(path = "/{code}")
    public Mono<GenericResponse<ProjectResponsePayload>> updateProject(@PathVariable String auth, @PathVariable Long code,
                                                                       @RequestBody ProjectRequestPayload projectPayload,
                                                                       ServerWebExchange exchange, Authentication authentication){
        return projectService.updateProject(code, projectPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+resp.getBody().getCode()));
    }

    @DeleteMapping(path = "/{code}")
    public Mono<GenericResponse<Void>> deleteProject(@PathVariable String auth, @PathVariable Long code, ServerWebExchange exchange, Authentication authentication){
        return projectService.deleteProject(code, exchange, authentication);
    }

    @GetMapping(path = "/{code}")
    public Mono<EntityModel<GenericResponse<ProjectResponsePayload>>> getProject(@PathVariable Long code, ServerWebExchange exchange){
        return projectService.getProject(code, exchange).flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<ProjectResponsePayload>>>> getProjects(@RequestParam(required = false) Boolean active,
                                                                                                   @RequestParam(required = false) Integer page,
                                                                                                   @RequestParam(required = false) Integer size,
                                                                                                   ServerWebExchange exchange){
        return assembler.toCollectionModel(projectService.getProjects(active, page, size, exchange), exchange);
    }

}
