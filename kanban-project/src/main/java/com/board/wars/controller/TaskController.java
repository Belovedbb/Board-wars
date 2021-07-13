package com.board.wars.controller;

import com.board.wars.GeneralUtil;
import com.board.wars.assembler.TaskModelAssembler;
import com.board.wars.payload.request.ProjectRequestPayload;
import com.board.wars.payload.request.TaskRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskResponsePayload;
import com.board.wars.service.TaskService;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/{auth}/kanban/project/{project-code}/column/{column-name}/task")
public class TaskController {

    private final TaskService taskService;
    private final TaskModelAssembler assembler;

    @Autowired
    public TaskController(TaskService taskService, TaskModelAssembler assembler) {
        this.taskService = taskService;
        this.assembler = assembler;
    }
//TODO model endpoint redirect for hatoas injection
    @PostMapping
    public Mono<GenericResponse<TaskResponsePayload>> createTask(@PathVariable("project-code") Long projectCode,
                                                                            @PathVariable("column-name") String columnName,
                                                                            @RequestBody TaskRequestPayload payload,
                                                                            @PathVariable String auth, ServerWebExchange exchange, Authentication authentication){
        return taskService.createTask(projectCode, columnName, payload, exchange, authentication);
                //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+code+"/task/"+resp.getBody().getTaskNo()));
    }

    @PatchMapping(path = "/{position}")
    public Mono<GenericResponse<TaskResponsePayload>> updateTask(@PathVariable String auth, @PathVariable("position") Long taskId,
                                                                    @PathVariable("project-code") Long projectCode, @RequestBody TaskRequestPayload payload,
                                                                    @PathVariable("column-name") String columnName, ServerWebExchange exchange, Authentication authentication){
        return taskService.updateTask(projectCode,columnName, taskId , payload, exchange, authentication);
                //.map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+resp.getBody().getProjectId()));
    }

    @DeleteMapping(path = "/{position}")
    public Mono<GenericResponse<Void>> deleteTask(@PathVariable String auth, @PathVariable("position") Long taskId,
                                                             @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                             ServerWebExchange exchange, Authentication authentication){
        return taskService.deleteTask(projectCode, columnName, taskId, exchange, authentication);
    }

    @GetMapping(path = "/{position}")
    public Mono<EntityModel<GenericResponse<TaskResponsePayload>>> getTask(@PathVariable("position") Long taskId,
                                                                                  @PathVariable("project-code") Long projectCode, @PathVariable("column-name") String columnName,
                                                                                  @PathVariable String auth, ServerWebExchange exchange){
        return taskService.getTask(projectCode, columnName, taskId, exchange)
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.COLUMN_NAME_KEY, columnName)))
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.TASK_ID_KEY, taskId)))
                .flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<TaskResponsePayload>>>> getTasks(@PathVariable("project-code") Long projectCode,
                                                                                                    @PathVariable("column-name") String columnName,
                                                                                                    ServerWebExchange exchange){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.COLUMN_NAME_KEY, columnName)))
                .flatMap(response -> assembler.toCollectionModel(taskService.getTasks(projectCode, columnName, exchange), exchange));
    }

    @GetMapping(path = "/{position}/move-to/column/{transfer-column-name}/task/{transfer-task-position}")
    public Mono<CollectionModel<EntityModel<GenericResponse<TaskResponsePayload>>>> moveTaskBetweenColumns(@PathVariable("position") Long taskId, @PathVariable("project-code") Long projectCode,
                                                                                                           @PathVariable("column-name") String columnName, @PathVariable("transfer-column-name") String transferColumnName,
                                                                                                           @PathVariable("transfer-task-position") Long transferTaskId, ServerWebExchange exchange,
                                                                                                           Authentication authentication) {
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.COLUMN_NAME_KEY, columnName)))
                .flatMap(response -> assembler.toCollectionModel(taskService.moveTaskBetweenColumns(projectCode, columnName, taskId, transferColumnName, transferTaskId, exchange, authentication), exchange));
    }

}
