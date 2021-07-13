package com.board.wars.service;

import com.board.wars.payload.request.SubTaskRequestPayload;
import com.board.wars.payload.response.SubTaskResponsePayload;
import com.board.wars.payload.response.GenericResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubTaskService {

    Mono<GenericResponse<SubTaskResponsePayload>> getSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId, ServerWebExchange exchange);

    Flux<GenericResponse<SubTaskResponsePayload>> getSubTasks(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange);

    Mono<GenericResponse<SubTaskResponsePayload>> createSubTask(Long projectCode, String columnName, Long taskId,
                                                                SubTaskRequestPayload subTaskPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<SubTaskResponsePayload>> updateSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId,
                                                                SubTaskRequestPayload subTaskPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteSubTask(Long projectCode, String columnName, Long taskId, Long subTaskId, ServerWebExchange exchange, Authentication authentication);
}
