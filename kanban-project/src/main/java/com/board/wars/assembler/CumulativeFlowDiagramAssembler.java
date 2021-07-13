package com.board.wars.assembler;

import com.board.wars.controller.GraphicsController;
import com.board.wars.domain.CumulativeFlowDiagram;
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
public class CumulativeFlowDiagramAssembler implements
        KanbanRepresentationModel<GenericResponse<CumulativeFlowDiagramResponsePayload>, EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>,
                        CumulativeFlowDiagram, EntityModel<CumulativeFlowDiagramResponsePayload>> {
    @Override
    public Mono<EntityModel<CumulativeFlowDiagramResponsePayload>> toSubModel(CumulativeFlowDiagram entity, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<CollectionModel<EntityModel<CumulativeFlowDiagramResponsePayload>>> toSubCollectionModel(Flux<? extends CumulativeFlowDiagram> entities, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>> toModel(GenericResponse<CumulativeFlowDiagramResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<CumulativeFlowDiagramResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange)).collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }


    private Tuple2<GenericResponse<CumulativeFlowDiagramResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<CumulativeFlowDiagramResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getCFD( payload.getBody().getProjectCode(),null), exchange)
                .withSelfRel()
                .toMono());
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getAllCFD( null,null, null), exchange)
                .withSelfRel()
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getCFDForAllDate(null, null,  null, null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(GraphicsController.class, exchange).getCFDForDateRange(null, null, null, null, null, null))
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }

}
