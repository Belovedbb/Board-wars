package com.board.wars.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.reactive.ReactiveRepresentationModelAssembler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KanbanRepresentationModel<S, T extends RepresentationModel<T>, U, V extends RepresentationModel<V>>
        extends ReactiveRepresentationModelAssembler<S, T> {

    Mono<V> toSubModel(U entity, ServerWebExchange exchange);

    Mono<CollectionModel<V>> toSubCollectionModel(Flux<? extends U> entities, ServerWebExchange exchange);

}
