package com.board.wars.assembler;

import com.board.wars.GeneralUtil;
import com.board.wars.controller.ColumnController;
import com.board.wars.domain.Column;
import com.board.wars.mapper.ColumnMapper;
import com.board.wars.payload.response.ColumnResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.util.KanbanRepresentationModel;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;

@Component
public class ColumnModelAssembler implements KanbanRepresentationModel<
        GenericResponse<ColumnResponsePayload>,
        EntityModel<GenericResponse<ColumnResponsePayload>>,
        Column,
        EntityModel<ColumnResponsePayload> > {

    final private ColumnMapper mapper;

    final private TaskModelAssembler taskAssembler;

    public ColumnModelAssembler(ColumnMapper mapper, TaskModelAssembler taskAssembler) {
        this.mapper = mapper;
        this.taskAssembler = taskAssembler;
    }

    @Override
    public Mono<EntityModel<ColumnResponsePayload>> toSubModel(Column entity, ServerWebExchange exchange) {
        return Mono.just(entity)
                .map(resource -> Tuples.of(mapper.mapColumnDomainToResponsePayload(resource), linkTo(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange)
                        .getColumn(exchange.getAttribute(Utilities.PROJECT_CODE_KEY ), resource.getName(),null), exchange)
                        .withSelfRel()
                        .toMono())
                )
                .flatMap(tuple -> {
                    ColumnResponsePayload payload = tuple.getT1();
                    return Mono.just(payload)
                            .filter(load -> !CollectionUtils.isEmpty(entity.getTasks()))
                            .flatMap(load -> taskAssembler.toSubCollectionModel(Flux.fromIterable(entity.getTasks()), setExchangeParams(exchange, entity.getName())))
                            .defaultIfEmpty(CollectionModel.empty())
                            .map(tasks -> {
                        payload.setTasks(tasks.hasLinks() ? tasks : null);
                        return Tuples.of(payload, tuple.getT2());
                    });
                })
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<ColumnResponsePayload>>> toSubCollectionModel(Flux<? extends Column> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toSubModel(entity, exchange))
                .collectList()
                .flatMap(resources ->
                        linkTo(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).getColumns( exchange.getAttribute(Utilities.PROJECT_CODE_KEY ),null, null, null, exchange), exchange)
                                .withSelfRel()
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).createColumn(null, null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).updateColumn(null, null,null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).deleteColumn(null, null,null, null, null))
                                .toMono()
                                .map(selfLink ->  CollectionModel.of(resources, selfLink))
                );
    }

    @Override
    public Mono<EntityModel<GenericResponse<ColumnResponsePayload>>> toModel(GenericResponse<ColumnResponsePayload> entity, ServerWebExchange exchange) {
        return Mono.just(entity)
                .map(resource -> Tuples.of(resource, linkTo(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange)
                        .getColumn(exchange.getAttribute(Utilities.PROJECT_CODE_KEY ), resource.getBody().getName(),null), exchange)
                        .withSelfRel()
                        .toMono())
                )
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<ColumnResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<ColumnResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.flatMap(entity -> toModel(entity, exchange))
                .collectList()
                .flatMap(resources ->
                        linkTo(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).getColumns( null,null, null, null, null), exchange)
                                .withSelfRel()
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).createColumn(null, null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).updateColumn(null, null,null, null, null, null))
                                .andAffordance(Utilities.HATEOAS.getInjectedMethod(ColumnController.class, exchange).deleteColumn(null, null,null, null, null))
                                .toMono()
                                .map(selfLink ->  CollectionModel.of(resources, selfLink))
                );
    }

    private ServerWebExchange setExchangeParams(ServerWebExchange exchange, String columnName) {
        return GeneralUtil.setAndGet(exchange, exchange1 -> exchange1.getAttributes().put(Utilities.COLUMN_NAME_KEY, columnName ));
    }

}
