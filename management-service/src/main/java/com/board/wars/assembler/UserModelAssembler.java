package com.board.wars.assembler;

import com.board.wars.controller.UserController;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.UserResponsePayload;
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
public class UserModelAssembler implements
        ReactiveRepresentationModelAssembler<GenericResponse<UserResponsePayload>, EntityModel<GenericResponse<UserResponsePayload>>> {

    @Override
    public Mono<EntityModel<GenericResponse<UserResponsePayload>>> toModel(GenericResponse<UserResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload)
                .map(resource -> Tuples.of(resource, linkTo(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).getUser( resource.getBody().getUsername(),null, null), exchange)
                        .withSelfRel()
                        .toMono())
                )
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<UserResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<UserResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange))
                .collectList()
                .flatMap(resources ->
                        linkTo(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).getUsers( null,null, null, null, null), exchange)
                                .withSelfRel()
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).updateUser(null, null,null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).deleteUser(null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).getProfilePicture(exchange, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(UserController.class, exchange).updateUserPicture(null, null, null, null))
                                .toMono()
                                .map(selfLink ->  CollectionModel.of(resources, selfLink))
                );
    }
}
