package com.board.wars.service;

import com.board.wars.payload.request.TaskCommentRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskCommentResponsePayload;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskCommentService {

    Mono<GenericResponse<TaskCommentResponsePayload>> getTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId, ServerWebExchange exchange);

    Flux<GenericResponse<TaskCommentResponsePayload>> getTaskComments(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange);

    Mono<GenericResponse<TaskCommentResponsePayload>> createTaskComment(Long projectCode, String columnName, Long taskId,
                                                                        TaskCommentRequestPayload taskCommentPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<TaskCommentResponsePayload>> updateTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId, TaskCommentRequestPayload taskCommentPayload,
                                                                        ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteTaskComment(Long projectCode, String columnName, Long taskId, Long taskCommentId,
                                                  ServerWebExchange exchange, Authentication authentication);

}
