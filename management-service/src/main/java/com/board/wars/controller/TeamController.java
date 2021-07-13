package com.board.wars.controller;

import com.board.wars.assembler.TeamModelAssembler;
import com.board.wars.payload.request.TeamRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.TeamResponsePayload;
import com.board.wars.service.TeamService;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping(path = "/{auth}/management/team")
public class TeamController {

    private final TeamService teamService;
    private final TeamModelAssembler assembler;

    public TeamController(TeamService teamService, TeamModelAssembler assembler) {
        this.teamService = teamService;
        this.assembler = assembler;
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<TeamResponsePayload>>>> getTeams(@RequestParam(required = false) Boolean active,
                                                                                             @RequestParam(required = false) Integer page,
                                                                                             @RequestParam(required = false) Integer size,
                                                                                             ServerWebExchange exchange, Authentication authentication
    ){
        return assembler.toCollectionModel(teamService.getTeams(exchange, authentication, active, page, size), exchange);
    }

    @PostMapping
    public Mono<GenericResponse<TeamResponsePayload>> createTeam(@PathVariable String auth,
                                                                 @RequestBody TeamRequestPayload teamPayload,
                                                                 ServerWebExchange exchange, Authentication authentication){
        return teamService.createTeam(teamPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/management/team/"+resp.getBody().getCode()));
    }

    @GetMapping(path = "/{code}")
    public Mono<EntityModel<GenericResponse<TeamResponsePayload>>> getTeam(@PathVariable String code, ServerWebExchange exchange,
                                                                           Authentication authentication){
        return teamService.getTeam(exchange, authentication, code).flatMap(response -> assembler.toModel(response, exchange));
    }

    @PatchMapping(path = "/{code}")
    public Mono<GenericResponse<TeamResponsePayload>> updateTeam(@PathVariable String auth, @PathVariable String code,
                                                                 @RequestBody TeamRequestPayload teamPayload,
                                                                 ServerWebExchange exchange, Authentication authentication){
        return teamService.updateTeam(code, teamPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/management/team/"+resp.getBody().getCode()));
    }

    @GetMapping(path = "/user/{user}")
    public Mono<CollectionModel<EntityModel<GenericResponse<TeamResponsePayload>>>> getUserTeams(@PathVariable String user,
                                                                                                 @RequestParam(required = false) Boolean active,
                                                                                                 @RequestParam(required = false) Integer page,
                                                                                                 @RequestParam(required = false) Integer size,
                                                                                                 ServerWebExchange exchange, Authentication authentication){
        return assembler.toCollectionModel(teamService.getUserTeams(user, exchange, authentication, active, page, size), exchange);
    }

    @DeleteMapping(path = "/{username}")
    public Mono<GenericResponse<Void>> deleteTeam(@PathVariable String auth, @PathVariable String username, Authentication authentication, ServerHttpRequest request){
        return  teamService.deleteTeam(username, authentication, request);
    }

}
