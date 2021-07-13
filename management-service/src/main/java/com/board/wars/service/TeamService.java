package com.board.wars.service;

import com.board.wars.payload.request.TeamRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TeamResponsePayload;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeamService {

    Mono<GenericResponse<TeamResponsePayload>> getTeam(ServerWebExchange webExchange, Authentication authentication, String code);

    Flux<GenericResponse<TeamResponsePayload>> getTeams(ServerWebExchange webExchange, Authentication authentication, Boolean active, Integer page, Integer pageSize);

    Flux<GenericResponse<TeamResponsePayload>> getUserTeams(String username, ServerWebExchange webExchange, Authentication authentication, Boolean active, Integer page, Integer pageSize);

    Mono<GenericResponse<TeamResponsePayload>> updateTeam(String code, TeamRequestPayload teamPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<TeamResponsePayload>> createTeam(TeamRequestPayload teamPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteTeam(String code, Authentication authentication, ServerHttpRequest request);

}
