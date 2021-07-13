package com.board.wars.controller;

import com.board.wars.GeneralUtil;
import com.board.wars.assembler.SubTaskModelAssembler;
import com.board.wars.payload.request.SubTaskRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.SubTaskResponsePayload;
import com.board.wars.service.SubTaskService;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/{auth}/kanban/project/{project-code}/column/{column-name}/task/{position}/subtask")
public class SubTaskController {

    private final SubTaskService subTaskService;
    private final SubTaskModelAssembler assembler;

    @Autowired
    public SubTaskController(SubTaskService subTaskService, SubTaskModelAssembler assembler) {
        this.subTaskService = subTaskService;
        this.assembler = assembler;
    }

    @PostMapping
    public Mono<GenericResponse<SubTaskResponsePayload>> createSubTask(@PathVariable("project-code") Long projectCode,
                                                                 @PathVariable("column-name") String columnName,
                                                                 @PathVariable("position") Long taskId,
                                                                 @RequestBody SubTaskRequestPayload payload,
                                                                 @PathVariable String auth, ServerWebExchange exchange,
                                                                       Authentication authentication){
        return subTaskService.createSubTask(projectCode, columnName, taskId, payload, exchange, authentication);
        //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+code+"/subTask/"+resp.getBody().getSubTaskNo()));
    }

    @PatchMapping(path = "/{subtask-id}")
    public Mono<GenericResponse<SubTaskResponsePayload>> updateSubTask(@PathVariable String auth, @PathVariable("subtask-id") Long subTaskId,
                                                                 @PathVariable("project-code") Long projectCode, @PathVariable("position") Long taskId, @RequestBody SubTaskRequestPayload payload,
                                                                 @PathVariable("column-name") String columnName, ServerWebExchange exchange, Authentication authentication){
        return subTaskService.updateSubTask(projectCode,columnName, taskId, subTaskId , payload, exchange, authentication);
        //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+resp.getBody().getProjectId()));
    }

    @DeleteMapping(path = "/{subtask-id}")
    public Mono<GenericResponse<Void>> deleteSubTask(@PathVariable String auth, @PathVariable("subtask-id") Long subTaskId,
                                                     @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                     @PathVariable("position") Long taskId, ServerWebExchange exchange, Authentication authentication){
        return subTaskService.deleteSubTask(projectCode, columnName, taskId, subTaskId, exchange, authentication);
    }

    @GetMapping(path = "/{subtask-id}")
    public Mono<EntityModel<GenericResponse<SubTaskResponsePayload>>> getSubTask(@PathVariable("subtask-id") Long subTaskId,
                                                                                 @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                                                 @PathVariable("position") Long taskId, @PathVariable String auth, ServerWebExchange exchange){
        return subTaskService.getSubTask(projectCode, columnName, taskId, subTaskId, exchange)
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.COLUMN_NAME_KEY, columnName)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.TASK_ID_KEY, taskId)))
                .flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<SubTaskResponsePayload>>>> getSubTasks(@PathVariable("project-code") Long projectCode,
                                                                                                   @PathVariable("column-name") String columnName,
                                                                                                   @PathVariable("position") Long taskId, ServerWebExchange exchange){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.COLUMN_NAME_KEY, columnName)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.TASK_ID_KEY, taskId)))
                .flatMap(response -> assembler.toCollectionModel(subTaskService.getSubTasks(projectCode, columnName, taskId, exchange), exchange));
    }
}
