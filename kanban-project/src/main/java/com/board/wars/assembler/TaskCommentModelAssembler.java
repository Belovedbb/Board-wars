package com.board.wars.assembler;

import com.board.wars.controller.TaskCommentController;
import com.board.wars.domain.TaskComment;
import com.board.wars.mapper.TaskCommentMapper;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskCommentResponsePayload;
import com.board.wars.util.KanbanRepresentationModel;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
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
public class TaskCommentModelAssembler implements KanbanRepresentationModel<GenericResponse<TaskCommentResponsePayload>,
        EntityModel<GenericResponse<TaskCommentResponsePayload>>, TaskComment, EntityModel<TaskCommentResponsePayload>> {

    private final TaskCommentMapper mapper;

    public TaskCommentModelAssembler(TaskCommentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<EntityModel<GenericResponse<TaskCommentResponsePayload>>> toModel(GenericResponse<TaskCommentResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<TaskCommentResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<TaskCommentResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    @Override
    public Mono<EntityModel<TaskCommentResponsePayload>> toSubModel(TaskComment entity, ServerWebExchange exchange) {
        return Mono.just(entity)
                .map(resource -> Tuples.of(mapper.mapTaskCommentDomainToResponsePayload(resource), linkTo(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange)
                        .getTaskComment(entity.getCode(), exchange.getAttribute(Utilities.PROJECT_CODE_KEY),
                                exchange.getAttribute(Utilities.COLUMN_NAME_KEY ), exchange.getAttribute(Utilities.TASK_ID_KEY ),null, exchange),
                        exchange)
                        .withSelfRel()
                        .toMono())
                )
                .map(tuple -> {
                    //Todo do next assembler
                    return tuple;
                })
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<TaskCommentResponsePayload>>> toSubCollectionModel(Flux<? extends TaskComment> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toSubModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange).getTaskComments(null, null, null, exchange), exchange)
                .withSelfRel()
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange).getTaskComment(null, null, null,null, null,   exchange))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange).createTaskComment(null, null, null, null, null, null, null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange).deleteTaskComment(null, null, null, null, null, null, null))
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }

    private Tuple2<GenericResponse<TaskCommentResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<TaskCommentResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(TaskCommentController.class, exchange)
                .getTaskComment(payload.getBody() == null ? null: payload.getBody().getCode(), exchange.getAttribute(Utilities.PROJECT_CODE_KEY),
                        exchange.getAttribute(Utilities.COLUMN_NAME_KEY ), exchange.getAttribute(Utilities.TASK_ID_KEY ),null, exchange), exchange)
                .withSelfRel()
                .toMono());
    }

}

