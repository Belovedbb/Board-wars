package com.board.wars.controller;

import com.board.wars.GeneralUtil;
import com.board.wars.assembler.TaskCommentModelAssembler;
import com.board.wars.payload.request.TaskCommentRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskCommentResponsePayload;
import com.board.wars.service.TaskCommentService;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/{auth}/kanban/project/{project-code}/column/{column-name}/task/{position}/task-comment")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;
    private final TaskCommentModelAssembler assembler;

    @Autowired
    public TaskCommentController(TaskCommentService taskCommentService, TaskCommentModelAssembler assembler) {
        this.taskCommentService = taskCommentService;
        this.assembler = assembler;
    }

    @PostMapping
    public Mono<GenericResponse<TaskCommentResponsePayload>> createTaskComment(@PathVariable("project-code") Long projectCode,
                                                                 @PathVariable("column-name") String columnName,
                                                                 @RequestBody TaskCommentRequestPayload payload,
                                                                               @PathVariable("position") Long taskId,
                                                                 @PathVariable String auth, ServerWebExchange exchange, Authentication authentication){
        return taskCommentService.createTaskComment(projectCode, columnName, taskId, payload, exchange, authentication);
        //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+code+"/taskComment/"+resp.getBody().getTaskCommentNo()));
    }

    @PatchMapping(path = "/{comment-id}")
    public Mono<GenericResponse<TaskCommentResponsePayload>> updateTaskComment(@PathVariable String auth, @PathVariable("comment-id") Long taskCommentId,
                                                                 @PathVariable("project-code") Long projectCode, @RequestBody TaskCommentRequestPayload payload,
                                                                 @PathVariable("column-name") String columnName, @PathVariable("position") Long taskId,
                                                                               ServerWebExchange exchange, Authentication authentication){
        return taskCommentService.updateTaskComment(projectCode,columnName, taskId, taskCommentId, payload, exchange, authentication);
        //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+resp.getBody().getProjectId()));
    }

    @DeleteMapping(path = "/{comment-id}")
    public Mono<GenericResponse<Void>> deleteTaskComment(@PathVariable String auth, @PathVariable("comment-id") Long taskCommentId,
                                                  @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                         @PathVariable("position") Long taskId, ServerWebExchange exchange, Authentication authentication){
        return taskCommentService.deleteTaskComment(projectCode, columnName, taskId, taskCommentId,  exchange, authentication);
    }

    @GetMapping(path = "/{comment-id}")
    public Mono<EntityModel<GenericResponse<TaskCommentResponsePayload>>> getTaskComment(@PathVariable("comment-id") Long taskCommentId,
                                                                           @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                                                         @PathVariable("position") Long taskId,
                                                                           @PathVariable String auth, ServerWebExchange exchange){
        return taskCommentService.getTaskComment(projectCode, columnName, taskId, taskCommentId, exchange)
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.COLUMN_NAME_KEY, columnName)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.TASK_ID_KEY, taskId)))
                .flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<TaskCommentResponsePayload>>>> getTaskComments(@PathVariable("project-code") Long projectCode,
                                                                                             @PathVariable("column-name") String columnName,
                                                                                                           @PathVariable("position") Long taskId,
                                                                                             ServerWebExchange exchange){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.COLUMN_NAME_KEY, columnName)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.TASK_ID_KEY, taskId)))
                .flatMap(response -> assembler.toCollectionModel(taskCommentService.getTaskComments(projectCode, columnName, taskId, exchange), exchange));
    }
}
