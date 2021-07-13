package com.board.wars.controller;

import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.security.Principal;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {

    final AuthService thisService;

    public AuthController(AuthService thisService) {
        this.thisService = thisService;
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @GetMapping(path = "/error")
    Flux<AuthenticatedResponse> authErrorHandler(@RequestParam(required = false) String error){
        return thisService.getAuthErrorHandler(error);
    }

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @GetMapping(path = "/error/organization")
    Flux<AuthenticatedResponse> authOrganizationErrorHandler(){
        return thisService.getAuthOrganizationErrorHandler();
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = {"/link/{type}/{dest}", "/link/{type}"})
    Mono<AuthenticatedResponse> authLinkHandler(@Valid @PathVariable @NotBlank String type, @PathVariable(required = false) String dest){
        return  thisService.getAuthLinkHandler(type, dest);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "/oauth2/code/github")
    Mono<AuthenticatedResponse> oauth2GithubRedirectHandler( @RequestParam String code, @RequestParam String state, ServerWebExchange exchange){
        return  thisService.oauth2GithubRedirectHandler(exchange);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "/state")
    Mono<AuthenticatedResponse> authState(ServerWebExchange exchange){
        return thisService.getAuthState(exchange);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "/state/type")
    Mono<AuthenticatedResponse> authStateType(OAuth2AuthenticationToken token, ServerWebExchange exchange){
        return thisService.getAuthStateType(token, exchange);
    }
}
