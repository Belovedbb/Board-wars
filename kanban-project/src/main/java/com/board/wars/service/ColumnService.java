package com.board.wars.service;

import com.board.wars.payload.request.ColumnRequestPayload;
import com.board.wars.payload.response.ColumnResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ColumnService {

    Mono<GenericResponse<ColumnResponsePayload>> getColumnProject(Long projectCode, String name, ServerWebExchange exchange);

    Flux<GenericResponse<ColumnResponsePayload>> getColumnsFromProject(Long projectCode, ServerWebExchange exchange);

    Mono<GenericResponse<ColumnResponsePayload>> createColumnForProject(Long projectCode, ColumnRequestPayload columnPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<ColumnResponsePayload>> updateColumnForProject(Long projectCode, String name, ColumnRequestPayload columnPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteColumnForProject(Long projectId, String name, ServerWebExchange exchange, Authentication authentication);

    Flux<GenericResponse<ColumnResponsePayload>> batchCreateColumnForProject(Long projectCode, ColumnRequestPayload[] columnPayload, ServerWebExchange exchange, Authentication authentication);

    Flux<GenericResponse<ColumnResponsePayload>> swapColumn(Long projectCode, final String firstColumnName, final String secondColumnName, ServerWebExchange exchange, Authentication authentication);

    Flux<GenericResponse<ColumnResponsePayload>> moveColumn(Long projectCode, String name, int position, ServerWebExchange exchange, Authentication authentication);
}
