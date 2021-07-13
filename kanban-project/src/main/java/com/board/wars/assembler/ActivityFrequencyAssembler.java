package com.board.wars.assembler;


import com.board.wars.controller.GraphicsController;
import com.board.wars.domain.ActivityFrequencyData;
import com.board.wars.payload.response.ActivityFrequencyDataResponsePayload;
import com.board.wars.payload.response.CumulativeFlowDiagramResponsePayload;
import com.board.wars.payload.response.GenericResponse;
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
public class ActivityFrequencyAssembler implements
        KanbanRepresentationModel<GenericResponse<ActivityFrequencyDataResponsePayload>, EntityModel<GenericResponse<ActivityFrequencyDataResponsePayload>>,
                ActivityFrequencyData, EntityModel<ActivityFrequencyDataResponsePayload>> {

    @Override
    public Mono<EntityModel<ActivityFrequencyDataResponsePayload>> toSubModel(ActivityFrequencyData entity, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<CollectionModel<EntityModel<ActivityFrequencyDataResponsePayload>>> toSubCollectionModel(Flux<? extends ActivityFrequencyData> entities, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<EntityModel<GenericResponse<ActivityFrequencyDataResponsePayload>>> toModel(GenericResponse<ActivityFrequencyDataResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<ActivityFrequencyDataResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<ActivityFrequencyDataResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    private Tuple2<GenericResponse<ActivityFrequencyDataResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<ActivityFrequencyDataResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getActivityFrequency( payload.getBody().getTitle(),null), exchange)
                .withSelfRel()
                .toMono());
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getActivityFrequency( null,  exchange), exchange)
                .withSelfRel()
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }
}
