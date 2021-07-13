package com.board.wars.controller;

import com.board.wars.assembler.ActivityFrequencyAssembler;
import com.board.wars.assembler.CumulativeFlowDiagramAssembler;
import com.board.wars.payload.response.ActivityFrequencyDataResponsePayload;
import com.board.wars.payload.response.CumulativeFlowDiagramResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.service.GraphicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/{auth}/kanban/graphics")
public class GraphicsController {

    private final GraphicsService graphicsService;
    private final CumulativeFlowDiagramAssembler cumulativeFlowDiagramAssembler;
    private final ActivityFrequencyAssembler activityFrequencyAssembler;

    public GraphicsController(GraphicsService graphicsService, CumulativeFlowDiagramAssembler cumulativeFlowDiagramAssembler, ActivityFrequencyAssembler activityFrequencyAssembler) {
        this.graphicsService = graphicsService;
        this.cumulativeFlowDiagramAssembler = cumulativeFlowDiagramAssembler;
        this.activityFrequencyAssembler = activityFrequencyAssembler;
    }

    @GetMapping("/cumulative-flow-diagram/project/{project-code}/filter")
    public Mono<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>> getCFDForAllDate(@PathVariable(value = "project-code") Long projectCode,
                                                                                                                @RequestParam(required = false) Integer page,
                                                                                                                @RequestParam(required = false) Integer size,
                                                                                                                ServerWebExchange exchange){
        return graphicsService.getCFDForAllDate(projectCode,  page, size, exchange).flatMap(response -> cumulativeFlowDiagramAssembler.toModel(response, exchange));
    }

    @GetMapping("/cumulative-flow-diagram/project/{project-code}/filter/start/{start-date}/end/{end-date}")
    public Mono<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>> getCFDForDateRange(@PathVariable(value = "project-code") Long projectCode,
                                                                                                            @RequestParam(required = false) Integer page,
                                                                                                            @RequestParam(required = false) Integer size,
                                                                                                            @PathVariable(value = "start-date") LocalDate startDate,
                                                                                                            @PathVariable(value = "end-date") LocalDate endDate,
                                                                                                            ServerWebExchange exchange){
        return graphicsService.getCFDForDateRange(projectCode,  page, size, startDate, endDate, exchange).flatMap(response -> cumulativeFlowDiagramAssembler.toModel(response, exchange));
    }

    @GetMapping("/cumulative-flow-diagram/project")
    public Mono<CollectionModel<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>>> getAllCFD(@RequestParam(required = false) Integer page,
                                                                                                               @RequestParam(required = false) Integer size,
                                                                                                               ServerWebExchange exchange){
        return cumulativeFlowDiagramAssembler.toCollectionModel(graphicsService.getAllCFD( page, size, exchange), exchange);
    }

    @GetMapping("/cumulative-flow-diagram/project/{project-code}")
    public Mono<EntityModel<GenericResponse<CumulativeFlowDiagramResponsePayload>>> getCFD(@PathVariable(value = "project-code") Long projectCode,
                                                                                                            ServerWebExchange exchange){
        return graphicsService.getCFD(projectCode, exchange).flatMap(response -> cumulativeFlowDiagramAssembler.toModel(response, exchange));
    }

    @GetMapping("/activity-frequency/{year}")
    public Mono<CollectionModel<EntityModel<GenericResponse<ActivityFrequencyDataResponsePayload>>>> getActivityFrequency(@PathVariable("year") String year, ServerWebExchange exchange) {
        return activityFrequencyAssembler.toCollectionModel(graphicsService.getActivityFrequency(year, exchange), exchange);
    }

    @GetMapping("/activity-frequency-points/{year}")
    public Flux<Long> getActivityFrequencyChartTaskDataPoint(@PathVariable("year") String year, ServerWebExchange exchange) {
        return graphicsService.getActivityFrequencyChartTaskDataPoint(year, exchange);
    }

}
