package com.board.wars.service;

import com.board.wars.payload.request.TaskRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TaskResponsePayload;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {

    Mono<GenericResponse<TaskResponsePayload>> getTask(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange);
    
    Flux<GenericResponse<TaskResponsePayload>> getTasks(Long projectCode, String columnName, ServerWebExchange exchange);
    
    Mono<GenericResponse<TaskResponsePayload>> createTask(Long projectCode, String columnName,  TaskRequestPayload taskPayload, ServerWebExchange exchange, Authentication authentication);
    
    Mono<GenericResponse<TaskResponsePayload>> updateTask(Long projectCode, String columnName, Long taskId, TaskRequestPayload taskPayload, ServerWebExchange exchange, Authentication authentication);
    
    Mono<GenericResponse<Void>> deleteTask(Long projectCode, String columnName, Long taskId, ServerWebExchange exchange, Authentication authentication);

    Flux<GenericResponse<TaskResponsePayload>> moveTaskBetweenColumns(Long projectCode, String columnName, Long taskId, String transferColumnName,
                                                                      Long transferTaskId, ServerWebExchange exchange, Authentication authentication);
    
    public Mono<GenericResponse<Void>> getTaskState(Long projectCode, Long id);
}

