package com.board.wars.assembler;

import com.board.wars.controller.SubTaskController;
import com.board.wars.domain.SubTask;
import com.board.wars.mapper.SubTaskMapper;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.SubTaskResponsePayload;
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
public class SubTaskModelAssembler implements KanbanRepresentationModel<GenericResponse<SubTaskResponsePayload>,
        EntityModel<GenericResponse<SubTaskResponsePayload>>, SubTask, EntityModel<SubTaskResponsePayload>> {

    private final SubTaskMapper mapper;

    public SubTaskModelAssembler(SubTaskMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Mono<EntityModel<GenericResponse<SubTaskResponsePayload>>> toModel(GenericResponse<SubTaskResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<SubTaskResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<SubTaskResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    @Override
    public Mono<EntityModel<SubTaskResponsePayload>> toSubModel(SubTask entity, ServerWebExchange exchange) {
        return Mono.just(entity)
                .map(resource -> Tuples.of(mapper.mapSubTaskDomainToResponsePayload(resource), linkTo(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange)
                        .getSubTask(entity.getCode(), exchange.getAttribute(Utilities.PROJECT_CODE_KEY), exchange.getAttribute(Utilities.COLUMN_NAME_KEY ), exchange.getAttribute(Utilities.TASK_ID_KEY),null, exchange), exchange)
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
    public Mono<CollectionModel<EntityModel<SubTaskResponsePayload>>> toSubCollectionModel(Flux<? extends SubTask> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toSubModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange).getSubTasks(null, null,null, exchange), exchange)
                .withSelfRel()
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange).getSubTask(null, null, null, null, null, exchange))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange).createSubTask(null, null, null, null, null,null,null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange).deleteSubTask(null, null, null, null, null, null,null))
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }

    private Tuple2<GenericResponse<SubTaskResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<SubTaskResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(SubTaskController.class, exchange)
                .getSubTask(exchange.getAttribute(Utilities.SUB_TASK_ID_KEY), exchange.getAttribute(Utilities.PROJECT_CODE_KEY),
                        exchange.getAttribute(Utilities.COLUMN_NAME_KEY ), exchange.getAttribute(Utilities.TASK_ID_KEY ), null, exchange), exchange)
                .withSelfRel()
                .toMono());
    }

}

