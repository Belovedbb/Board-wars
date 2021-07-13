package com.board.wars.controller;

import com.board.wars.GeneralUtil;
import com.board.wars.assembler.ColumnModelAssembler;
import com.board.wars.payload.request.ColumnRequestPayload;
import com.board.wars.payload.response.ColumnResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.service.ColumnService;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/{auth}/kanban/project/{project-code}/column")
public class ColumnController {

    final private ColumnService columnService;
    private final ColumnModelAssembler assembler;

    public ColumnController(ColumnService columnService, ColumnModelAssembler assembler) {
        this.columnService = columnService;
        this.assembler = assembler;
    }

    @PostMapping
    public Mono<GenericResponse<ColumnResponsePayload>> createColumn(@PathVariable String auth, @PathVariable(value = "project-code") Long projectCode,
                                                                     @RequestBody ColumnRequestPayload createColumnPayload, ServerWebExchange exchange, Authentication authentication){
        return columnService.createColumnForProject(projectCode, createColumnPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/kanban/project/"+projectCode + "/column/" + resp.getBody().getName()));
    }

    @PatchMapping(path = "/{name}")
    public Mono<GenericResponse<ColumnResponsePayload>> updateColumn(@PathVariable String auth, @PathVariable(value = "project-code") Long projectCode, @PathVariable String name,
                                                                       @RequestBody ColumnRequestPayload columnPayload,
                                                                       ServerWebExchange exchange, Authentication authentication){
        return columnService.updateColumnForProject(projectCode, name, columnPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) +  "/kanban/project/"+projectCode + "/column/" + name));
    }

    @DeleteMapping(path = "/{name}")
    public Mono<GenericResponse<Void>> deleteColumn(@PathVariable String auth,  @PathVariable(value = "project-code") Long projectCode,
                                                    @PathVariable String name, ServerWebExchange exchange, Authentication authentication){
        return columnService.deleteColumnForProject(projectCode, name, exchange, authentication);
    }

    @GetMapping(path = "/{name}")
    public Mono<EntityModel<GenericResponse<ColumnResponsePayload>>> getColumn( @PathVariable(value = "project-code") Long projectCode, @PathVariable String name, ServerWebExchange exchange){
        return columnService.getColumnProject(projectCode, name, exchange)
                .map(response -> GeneralUtil.setAndGet(response, resp -> exchange.getAttributes().put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<ColumnResponsePayload>>>> getColumns( @PathVariable(value = "project-code") Long projectCode,
                                                                                                  @RequestParam(required = false) Boolean active,
                                                                                                 @RequestParam(required = false) Integer page,
                                                                                                 @RequestParam(required = false) Integer size,
                                                                                                 ServerWebExchange exchange){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .flatMap(response -> assembler.toCollectionModel(columnService.getColumnsFromProject(projectCode, exchange), exchange));
    }

    @GetMapping(path = "/swap/{first}/{second}")
    public Mono<CollectionModel<EntityModel<GenericResponse<ColumnResponsePayload>>>> swapColumn( @PathVariable(value = "project-code") Long projectCode,
                                                                                                  @PathVariable(value = "first") String first,
                                                                                                  @PathVariable(value = "second") String second,
                                                                                                  ServerWebExchange exchange,  Authentication authentication){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .flatMap(response -> assembler.toCollectionModel(columnService.swapColumn(projectCode, first, second, exchange, authentication), exchange));
    }

    @GetMapping(path = "/move/{name}/{position}")
    public Mono<CollectionModel<EntityModel<GenericResponse<ColumnResponsePayload>>>> moveColumn( @PathVariable(value = "project-code") Long projectCode,
                                                                                                  @PathVariable(value = "name") String name,
                                                                                                  @PathVariable(value = "position") int position,
                                                                                                  ServerWebExchange exchange, Authentication authentication){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .flatMap(response -> assembler.toCollectionModel(columnService.moveColumn(projectCode, name, position, exchange, authentication), exchange));
    }

    @PostMapping(path = "/batch")
    public Mono<CollectionModel<EntityModel<GenericResponse<ColumnResponsePayload>>>> batchCreateColumnForProject( @PathVariable(value = "project-code") Long projectCode,
                                                                                                                   @RequestBody ColumnRequestPayload[] payloads,
                                                                                                                   ServerWebExchange exchange,  Authentication authentication){
        return Mono.just(exchange.getAttributes())
                .map(map -> GeneralUtil.setAndGet(map, resp -> resp.put(Utilities.PROJECT_CODE_KEY, projectCode)))
                .flatMap(response -> assembler.toCollectionModel(columnService.batchCreateColumnForProject(projectCode, payloads, exchange, authentication), exchange));
    }


}
