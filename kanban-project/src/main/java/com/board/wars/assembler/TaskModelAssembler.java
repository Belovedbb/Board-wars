package com.board.wars.assembler;

import com.board.wars.GeneralUtil;
import com.board.wars.controller.TaskController;
import com.board.wars.domain.Task;
import com.board.wars.mapper.TaskMapper;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskResponsePayload;
import com.board.wars.util.KanbanRepresentationModel;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;

@Component
public class TaskModelAssembler implements KanbanRepresentationModel<GenericResponse<TaskResponsePayload>,
                EntityModel<GenericResponse<TaskResponsePayload>>, Task, EntityModel<TaskResponsePayload>> {

    private final TaskMapper mapper;
    private final SubTaskModelAssembler subTaskModelAssembler;
    private final TaskCommentModelAssembler taskCommentModelAssembler;

    public TaskModelAssembler(TaskMapper mapper, SubTaskModelAssembler subTaskModelAssembler, TaskCommentModelAssembler taskCommentModelAssembler) {
        this.mapper = mapper;
        this.subTaskModelAssembler = subTaskModelAssembler;
        this.taskCommentModelAssembler = taskCommentModelAssembler;
    }

    @Override
    public Mono<EntityModel<GenericResponse<TaskResponsePayload>>> toModel(GenericResponse<TaskResponsePayload> payload, ServerWebExchange exchange) {
        return Mono.just(payload).map(resource -> mapElementsToTuple(resource, exchange)).flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<GenericResponse<TaskResponsePayload>>>> toCollectionModel(Flux<? extends GenericResponse<TaskResponsePayload>> entities, ServerWebExchange exchange) {
        return entities.index()
                .flatMap(entity -> toModel(entity.getT2(), GeneralUtil.setAndGet(exchange, exchange1 -> exchange1.getAttributes().put(Utilities.TASK_ID_KEY, entity.getT1()))))
                .collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    @Override
    public Mono<EntityModel<TaskResponsePayload>> toSubModel(Task entity, ServerWebExchange exchange) {
        return Mono.just(entity)
                .map(resource -> Tuples.of(mapper.mapTaskDomainToResponsePayload(resource), linkTo(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange)
                        .getTask(exchange.getAttribute(Utilities.TASK_ID_KEY), exchange.getAttribute(Utilities.PROJECT_CODE_KEY), exchange.getAttribute(Utilities.COLUMN_NAME_KEY ),null, exchange), exchange)
                        .withSelfRel()
                        .toMono())
                )
                .flatMap(tuple -> {
                    tuple.getT1().setPosition(exchange.getAttribute(Utilities.TASK_ID_KEY));
                    TaskResponsePayload payload = tuple.getT1();
                    return Mono.just(payload)
                            .filter(load -> !CollectionUtils.isEmpty(entity.getSubTasks()))
                            .flatMap(load -> subTaskModelAssembler.toSubCollectionModel(Flux.fromIterable(entity.getSubTasks()), setExchangeParams(exchange, entity.getName())))
                            .defaultIfEmpty(CollectionModel.empty())
                            .map(subTasks -> {
                                payload.setSubTasks(subTasks.hasLinks() ? subTasks : null);
                                return Tuples.of(payload, tuple.getT2());
                            });
                })
                .flatMap(tuple -> {
                    tuple.getT1().setPosition(exchange.getAttribute(Utilities.TASK_ID_KEY));
                    TaskResponsePayload payload = tuple.getT1();
                    return Mono.just(payload)
                            .filter(load -> !CollectionUtils.isEmpty(entity.getComments()))
                            .flatMap(load -> taskCommentModelAssembler.toSubCollectionModel(Flux.fromIterable(entity.getComments()), setExchangeParams(exchange, entity.getName())))
                            .defaultIfEmpty(CollectionModel.empty())
                            .map(comments -> {
                                payload.setComments(comments.hasLinks() ? comments : null);
                                return Tuples.of(payload, tuple.getT2());
                            });
                })
                .flatMap(tuple -> tuple.getT2().map(link -> EntityModel.of(tuple.getT1(), link)));
    }

    @Override
    public Mono<CollectionModel<EntityModel<TaskResponsePayload>>> toSubCollectionModel(Flux<? extends Task> entities, ServerWebExchange exchange) {
        return entities.index()
                .flatMap(entity -> toSubModel(entity.getT2(), GeneralUtil.setAndGet(exchange, exchange1 -> exchange1.getAttributes().put(Utilities.TASK_ID_KEY, entity.getT1()))))
                .collectList().flatMap(resources -> linkAffordances(resources, exchange));
    }

    private <T> Mono<CollectionModel<EntityModel<T>>> linkAffordances(List<EntityModel<T>> models, ServerWebExchange exchange) {
        return linkTo(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange).getTasks(null, null, exchange), exchange)
                .withSelfRel()
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange).getTask(null, null, null, null, exchange))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange).createTask(null, null, null, null,null, null))
                .andAffordance(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange).deleteTask(null, null, null, null, null,null))
                .toMono()
                .map(selfLink ->  CollectionModel.of(models, selfLink));
    }

    private Tuple2<GenericResponse<TaskResponsePayload>, Mono<Link>> mapElementsToTuple(GenericResponse<TaskResponsePayload> payload, ServerWebExchange exchange){
        return Tuples.of(payload, linkTo(Utilities.HATEOAS.getInjectedMethod(TaskController.class, exchange)
                .getTask(exchange.getAttribute(Utilities.TASK_ID_KEY), exchange.getAttribute(Utilities.PROJECT_CODE_KEY),
                        exchange.getAttribute(Utilities.COLUMN_NAME_KEY ),null, exchange), exchange)
                .withSelfRel()
                .toMono());
    }

    private ServerWebExchange setExchangeParams(ServerWebExchange exchange, String columnName) {
        return GeneralUtil.setAndGet(exchange, exchange1 -> exchange1.getAttributes().put(Utilities.COLUMN_NAME_KEY, columnName ));
    }
}

