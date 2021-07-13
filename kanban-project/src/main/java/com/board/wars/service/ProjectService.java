package com.board.wars.service;

import com.board.wars.payload.request.ProjectRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.ProjectResponsePayload;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectService {

    Mono<GenericResponse<ProjectResponsePayload>> getProject(Long code, ServerWebExchange exchange);

    Flux<GenericResponse<ProjectResponsePayload>> getProjects(Boolean active, Integer page, Integer pageSize, ServerWebExchange exchange);

    Mono<GenericResponse<ProjectResponsePayload>> createProject(ProjectRequestPayload project, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<ProjectResponsePayload>> updateProject(Long code, ProjectRequestPayload project, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteProject(Long code, ServerWebExchange exchange, Authentication authentication);

}
