package com.board.wars.controller;

import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.payload.marker.*;
import com.board.wars.service.MarkerService;
import com.board.wars.utils.RouteUtil;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequestMapping(path = "/marker")
public class GlobalMarkerController {

    final ServerRequestCache requestCache;
    final private MarkerService thisService;

    public GlobalMarkerController(MarkerService thisService, ServerRequestCache requestCache) {
        this.thisService = thisService;
        this.requestCache = requestCache;
    }

    @GetMapping("/success")
    public Flux<AuthenticatedResponse> globalMapperSaver(ServerHttpRequest request) {
        return thisService.saveMarker(request);
    }

    @GetMapping(value = "/{level}/helper")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Flux<AuthenticatedResponse> globalPartMapperHelper(@RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client, @PathVariable(required = false) AuthenticatedResponse.Level level){
        return thisService.helper(client, level);
    }

    @GetMapping(value = "/global")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Publisher<?> globalPartMapperSaver(@RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client, OAuth2AuthenticationToken authToken,
                                                   ServerHttpRequest request, ServerWebExchange exchange){
        return requestCache.getRedirectUri(exchange).defaultIfEmpty(URI.create(""))
                .flatMap(uri -> this.thisService.resolveGlobalDestination(client, authToken, request, exchange, uri, URI.create("")));
    }

    @PostMapping("/storage")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Flux<AuthenticatedResponse> storagePartMapperSaver(@RequestBody StorageMarkerPayload storageRequest, @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client, OAuth2AuthenticationToken authToken, ServerHttpRequest request){
        return thisService.storageService(client, authToken, request, storageRequest);
    }

    @PostMapping("/role")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Flux<AuthenticatedResponse> rolePartMapperSaver(@RequestBody RoleMarkerPayload roleRequest, @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client, OAuth2AuthenticationToken authToken, ServerHttpRequest request){
        return thisService.roleService(client, authToken, request, roleRequest);
    }

    @PostMapping("/token")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public Flux<AuthenticatedResponse> tokenPartMapperSaver(@RequestBody TokenMarkerPayload tokenRequest, @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient client, OAuth2AuthenticationToken authToken, ServerHttpRequest request){
        return thisService.tokenService(client, authToken, request, tokenRequest);
    }

    @GetMapping("/failure")
    public Flux<AuthenticatedResponse> globalMapperFailure() {
        return thisService.failureMarker();
    }

}
