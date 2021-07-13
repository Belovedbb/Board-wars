package com.board.wars.service;

import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.init.MarkerStarter;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.MarkerContainerUtil;
import com.board.wars.utils.RouteUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private static final String GATEWAY = "gateway";
    final MarkerStarter starter;
    final MarkerContainerUtil markerContainerUtil;

    private static final CookieResponseUtil cookieHelper = CookieResponseUtil.getInstance();

    public AuthService(MarkerStarter starter, MarkerContainerUtil markerContainerUtil) {
        this.starter = starter;
        this.markerContainerUtil = markerContainerUtil;
    }

    public Flux<AuthenticatedResponse> getAuthErrorHandler(String error){
        return Flux.just(new AuthenticatedResponse(false, "access denied, please consider logging in", AuthenticatedResponse.Level.PRIMARY, LocalDateTime.now()));
    }

    public Flux<AuthenticatedResponse> getAuthOrganizationErrorHandler(){
        return Flux.just(AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_GLOBAL, false));
    }

    public Mono<AuthenticatedResponse> getAuthLinkHandler(String type, String dest){
        AuthenticatedResponse response = new AuthenticatedResponse();
        response.setAdditionalInfo("/oauth2/authorization/" + type + (StringUtils.hasText(dest) && dest.equals("ui")? "?" + RouteUtil.Internal.AUTH_POPUP_RAW_QUERY : ""));
        return  Mono.just(response);
    }

    @CircuitBreaker(name = GATEWAY, fallbackMethod = "getDefaultLeveledResponse")
    @Bulkhead(name = GATEWAY)
    public Mono<AuthenticatedResponse> getAuthState(ServerWebExchange exchange){
        return getAuthState(starter, exchange.getPrincipal(), exchange.getRequest());
    }

    @CircuitBreaker(name = GATEWAY, fallbackMethod = "getDefaultLeveledResponse")
    @Bulkhead(name = GATEWAY)
    public Mono<AuthenticatedResponse> getAuthStateType(OAuth2AuthenticationToken token, ServerWebExchange exchange){
        return getAuthState(starter, exchange.getPrincipal(), exchange.getRequest()).map(response -> {
            String tokenType = token == null ? "not available" : token.getAuthorizedClientRegistrationId();
            response.setValue(tokenType);
            return response;
        });
    }

    public Mono<AuthenticatedResponse> oauth2GithubRedirectHandler(ServerWebExchange exchange){
        AuthenticatedResponse response = new AuthenticatedResponse();
        response.setAdditionalInfo(exchange.getRequest().getURI());
        //response.setAdditionalInfo("/oauth2/authorization/" + type);
        return  Mono.just(response);
    }

    private Mono<AuthenticatedResponse> getAuthState(MarkerStarter markerStarter, Mono<Principal> principal, ServerHttpRequest request){
        return markerStarter.getGlobalMarker()
                .defaultIfEmpty(new GlobalMarker())
                .map(marker -> getMarkerPrincipalContainer(principal, marker))
                .map(tuple -> Tuples.of(tuple.getT1(), tuple.getT2().defaultIfEmpty(() -> "")))
                .flatMap(tupleData -> getLeveledResponse(tupleData, request));
    }

    private Tuple2<AuthenticatedResponse.Level, Mono<Principal>> getMarkerPrincipalContainer(Mono<Principal> principal, GlobalMarker marker){
        AuthenticatedResponse.Level level = AuthenticatedResponse.Level.ORGANIZATION_FAILURE;
        Tuple2<AuthenticatedResponse.Level, Mono<Principal>> container = Tuples.of(level, Mono.just(() -> ""));
        if(StringUtils.hasText(marker.getId())){
            container = Tuples.of(AuthenticatedResponse.Level.ORGANIZATION_SUCCESS, principal);
        }
        return container;
    }

    //if there is no cookie value, consider it as a fresh registration
    private Mono<AuthenticatedResponse> getLeveledResponse(Tuple2<AuthenticatedResponse.Level, Mono<Principal>> tuple, ServerHttpRequest request){
        if(tuple.getT1().equals(AuthenticatedResponse.Level.ORGANIZATION_FAILURE)){
            Optional<ResponseCookie> cookie = cookieHelper.getCookieForAttribute(request.getHeaders().toSingleValueMap(), CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY, "Cookie");
            return markerContainerUtil.nextLevelMarker(cookie.orElseGet(cookieHelper.getDefaultCookie()).getValue())
                    .map(level -> level == AuthenticatedResponse.Level.ORGANIZATION_FAILURE ? AuthenticatedResponse.Level.ORGANIZATION_GLOBAL : level)
                    .map(level -> new AuthenticatedResponse(false, "null configuration", level, LocalDateTime.now()));
        }else{
            return tuple.getT2().map(principalAuth -> {
                if(principalAuth.getName().equals("")){
                    return new AuthenticatedResponse(false, "not logged in", AuthenticatedResponse.Level.PRIMARY_FAILURE, LocalDateTime.now());
                }else{
                    return new AuthenticatedResponse(true, "logged in", AuthenticatedResponse.Level.PRIMARY_SUCCESS, LocalDateTime.now());
                }
            });
        }
    }

    private Mono<AuthenticatedResponse> getDefaultLeveledResponse(Exception ex) {
        return Mono.just(new AuthenticatedResponse(false, "null configuration", AuthenticatedResponse.Level.PRIMARY_FAILURE, LocalDateTime.now()));
    }

}
