package com.board.wars.assembler;

import com.board.wars.controller.ProjectController;
import com.board.wars.domain.Project;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.ProjectResponsePayload;
import com.board.wars.util.KanbanRepresentationModel;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;

@Component
public class ProjectModelAssembler implements
        KanbanRepresentationModel<GenericResponse<ProjectResponsePayload>, EntityModel<GenericResponse<ProjectResponsePayload>>,
                Project, EntityModel<ProjectResponsePayload>> {

    @Override
    public Mono<EntityModel<GenericResponse<ProjectResponsePayload>>> toModel(GenericResponse<ProjectResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<ProjectResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<ProjectResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    private Tuple2<GenericResponse<ProjectResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<ProjectResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(ProjectController.class, exchange).getProject( payload.getBody().getCode(),null), exchange)
                .withSelfRel()
                .toMono());
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(ProjectController.class, exchange).getProjects( null,null, null, null), exchange)
                .withSelfRel()
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ProjectController.class, exchange).createProject(null, null, null, null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ProjectController.class, exchange).updateProject(null, null, null, null, null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ProjectController.class, exchange).deleteProject(null, null, null, null))
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }

    @Override
    public Mono<EntityModel<ProjectResponsePayload>> toSubModel(Project entity, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<CollectionModel<EntityModel<ProjectResponsePayload>>> toSubCollectionModel(Flux<? extends Project> entities, ServerWebExchange exchange) {
        return null;
    }
}
