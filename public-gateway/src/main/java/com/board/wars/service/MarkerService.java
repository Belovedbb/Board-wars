package com.board.wars.service;

import com.board.wars.domain.marker.OrganizationDetail;
import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.payload.marker.RoleMarkerPayload;
import com.board.wars.payload.marker.StorageMarkerPayload;
import com.board.wars.payload.marker.TokenMarkerPayload;
import com.board.wars.service.marker.GlobalMarkerService;
import com.board.wars.service.marker.MarkerPartService;
import com.board.wars.store.marker.GlobalMarkerRepository;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.RouteUtil;
import com.board.wars.utils.RouteUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;

@Service
public class MarkerService {

    private static final String GATEWAY = "gateway";

    private final ServerRequestCache requestCache;
    private final MarkerPartService globalMarkerPartService;
    private final MarkerPartService storageMarkerPartService;
    private final MarkerPartService roleMarkerPartService;
    private final MarkerPartService tokenMarkerPartService;
    private final GlobalMarkerService globalMarkerService;
    private final GlobalMarkerRepository globalMarkerRepository;

    public MarkerService(MarkerPartService globalMarkerPartService, MarkerPartService storageMarkerPartService,
                         MarkerPartService roleMarkerPartService, MarkerPartService tokenMarkerPartService,
                         GlobalMarkerService globalMarkerService, ServerRequestCache requestCache, GlobalMarkerRepository globalMarkerRepository) {
        this.globalMarkerPartService = globalMarkerPartService;
        this.storageMarkerPartService = storageMarkerPartService;
        this.roleMarkerPartService = roleMarkerPartService;
        this.tokenMarkerPartService = tokenMarkerPartService;
        this.globalMarkerService = globalMarkerService;
        this.requestCache = requestCache;
        this.globalMarkerRepository = globalMarkerRepository;
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> globalService(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request){
        return globalMarkerPartService.serve(client, token, request, null);
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> storageService(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request, StorageMarkerPayload storagePayload){
        return storageMarkerPartService.serve(client, token, request, storagePayload);
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> roleService(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request, RoleMarkerPayload rolePayload){
        return roleMarkerPartService.serve(client, token, request, rolePayload);
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> tokenService(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request, TokenMarkerPayload tokenPayload){
        return tokenMarkerPartService.serve(client, token, request, tokenPayload);
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> helper(OAuth2AuthorizedClient client, AuthenticatedResponse.Level level){
        return Mono.just(level)
                .defaultIfEmpty(AuthenticatedResponse.Level.ORGANIZATION_GLOBAL)
                .map(response -> AuthenticatedResponse.populateResponse(response, false))
                .flux();
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> saveMarker(ServerHttpRequest request){
        String currentToken = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        return globalMarkerService.serve(currentToken);
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Flux<AuthenticatedResponse> failureMarker(){
        return Flux.just(new AuthenticatedResponse(false, "an error occurred", AuthenticatedResponse.Level.PRIMARY, LocalDateTime.now()));
    }

    @CircuitBreaker(name = GATEWAY)
    @Bulkhead(name = GATEWAY)
    public Mono<?> resolveGlobalDestination(OAuth2AuthorizedClient client, OAuth2AuthenticationToken authToken, ServerHttpRequest request,
                                            ServerWebExchange exchange, URI uri, URI defaultUri){
        Mono<AuthenticatedResponse> authResp = globalService(client, authToken, request).next();
        if(uri.getPath().equals(defaultUri.getPath())){
            return authResp;
        }else{
            return requestCache.removeMatchingRequest(exchange)
                    .flatMap(req -> authResp)
                    .flatMap(resp -> RouteUtil.redirect(exchange.getResponse(), createPageLink(exchange.getRequest())));
        }
    }

    private URI createPageLink(ServerHttpRequest request){
        String host = RouteUtil.External.ENDPOINT_UI;
        String path = RouteUtil.External.ENDPOINT_UI_AUTH_PAGE;
        String authValue= request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_AUTH_BEARER_ID).getValue();
        String markerValue = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        URI uri = URI.create("");
        if(StringUtils.hasText(authValue) && StringUtils.hasText(markerValue)){
            StringBuilder builder = new StringBuilder();
            builder.append(host).append(path)
                    .append("?")
                    .append("status").append("=").append("SUCCESS");
            uri = URI.create(builder.toString());
        }
        return uri;
    }

    public Mono<GlobalMarker> updateGlobalMarker(GlobalMarker payload) {
        if(payload != null && StringUtils.hasText(payload.getEmail())) {
            return this.globalMarkerRepository.
                    findByEmail(payload.getEmail())
                    .map(globalMarker ->  {
                        if(StringUtils.hasText(payload.getOrganizationName())) {
                            globalMarker.setOrganizationName(payload.getOrganizationName());
                        }
                        if(payload.getOrganization() != null && StringUtils.hasText(payload.getOrganization().getDescription())) {
                            if(globalMarker.getOrganization() == null) globalMarker.setOrganization(new OrganizationDetail());
                            globalMarker.getOrganization().setDescription(payload.getOrganization().getDescription());
                        }
                        return globalMarker;
                    }).flatMap(globalMarkerRepository::save);
        }
        return Mono.empty();
    }

    public Mono<GlobalMarker> getGlobalMarkerObject(Mono<Principal> principal) {
        return principal
                .cast(OAuth2AuthenticationToken.class)
                .map(AbstractAuthenticationToken::getDetails)
                .cast(GlobalMarker.class).flatMap(marker -> globalMarkerRepository.findById(marker.getId()));
    }
}
