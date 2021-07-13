package com.board.wars.assembler;

import com.board.wars.controller.TeamController;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TeamResponsePayload;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.reactive.ReactiveRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;

@Component
public class TeamModelAssembler implements
        ReactiveRepresentationModelAssembler<GenericResponse<TeamResponsePayload>, EntityModel<GenericResponse<TeamResponsePayload>>> {

    @Override
    public Mono<EntityModel<GenericResponse<TeamResponsePayload>>> toModel(GenericResponse<TeamResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload)
                .map(resource -> Tuples.of(resource, linkTo(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange)
                        .getTeam( resource.getBody().getCode(),null, null), exchange)
                        .withSelfRel()
                        .toMono())
                )
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<TeamResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<TeamResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange))
                .collectList()
                .flatMap(resources ->
                        linkTo(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange).getTeams( null,null, null, null, null), exchange)
                                .withSelfRel()
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange).updateTeam(null, null,null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange).deleteTeam(null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange).createTeam(null, null, exchange, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TeamController.class, exchange).getUserTeams(null, null, null, null, null, null))
                                .toMono()
                                .map(selfLink ->  CollectionModel.of(resources, selfLink))
                );
    }
}
