package com.board.wars.service;

import com.board.wars.domain.CumulativeFlowDiagram;
import com.board.wars.payload.response.ActivityFrequencyDataResponsePayload;
import com.board.wars.payload.response.CumulativeFlowDiagramResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface GraphicsService {

    Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFDForAllDate(Long projectCode, Integer page, Integer pageSize, ServerWebExchange exchange);

    Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFDForDateRange(Long projectCode, Integer page, Integer size, LocalDate startDate, LocalDate endDate, ServerWebExchange exchange);

    Flux<GenericResponse<CumulativeFlowDiagramResponsePayload>> getAllCFD(Integer page, Integer pageSize, ServerWebExchange exchange);

    Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> getCFD(Long projectCode, ServerWebExchange exchange);

    Mono<GenericResponse<CumulativeFlowDiagramResponsePayload>> createCFD(Long projectCode, CumulativeFlowDiagram payload);

    Flux<GenericResponse<ActivityFrequencyDataResponsePayload>> getActivityFrequency(String year, ServerWebExchange exchange) ;

    Flux<Long> getActivityFrequencyChartTaskDataPoint(String year, ServerWebExchange exchange);

}
