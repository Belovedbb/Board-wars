package com.board.wars.config.filter;


import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.init.MarkerStarter;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.MarkerContainerUtil;
import com.board.wars.utils.RouteUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Supplier;

import static com.board.wars.config.auth.AuthConfiguration.WHITELIST_RESOURCE_PATHS;
import static com.board.wars.config.auth.AuthConfiguration.WHITELIST_URL_PATHS;

//TODO restrict filter to certain routes
@Component
public class GlobalMarkerFilter implements WebFilter {
    /*
    -if organization global marker hasnt been created, redirect to error with hash
    -if local auth is used, unvalidate @authsuccessfulEntry and redirect to error with hash
    -if auth is github, get next step and redirect
     */
    private final static String baseOperationUrlPath = "/auth/error/organization";
    final private MarkerStarter markerStarter;
    private final MarkerContainerUtil markerContainerUtil;
    private static final CookieResponseUtil cookieHelper = CookieResponseUtil.getInstance();

    public GlobalMarkerFilter(MarkerStarter markerStarter, MarkerContainerUtil markerContainerUtil) {
        this.markerStarter = markerStarter;
        this.markerContainerUtil = markerContainerUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return markerStarter.getGlobalMarker()
                .switchIfEmpty(Mono.just(new GlobalMarker()))
                .flatMap(marker -> mainMapper(marker, exchange))
                .flatMap(chain::filter);
    }

    private Mono<ServerWebExchange> mainMapper(GlobalMarker marker, ServerWebExchange exchange){
        return bypassMarkerCheckCondition(marker, exchange)
                .filter(result -> result)
                .flatMap(result -> Mono.just(exchange))
                .switchIfEmpty(defineFilterMapper(exchange));
    }

    private Mono<ServerWebExchange> defineFilterMapper(ServerWebExchange exchange){
        return exchange.getPrincipal()
                .defaultIfEmpty(defaultPrincipal().get())
                .flatMap(principal -> chainNextLevelMarker(exchange))
                .defaultIfEmpty(Tuples.of(defaultPrincipal().get(), exchange))
                .flatMap(this::chainEmptyAuthMarker);
    }

    private Mono<Tuple2<Principal, ServerWebExchange>> chainNextLevelMarker(ServerWebExchange exchange){
        return exchange.getPrincipal()
                .filter(this::isMainOrganizationAuthenticated)
                .flatMap(principal -> nextLevelMarkerRedirector(principal, exchange));
    }

    private Mono<ServerWebExchange> chainEmptyAuthMarker(Tuple2<Principal, ServerWebExchange> principalExchangeTuple){
        Principal principal = principalExchangeTuple.getT1();
        ServerWebExchange exchange = principalExchangeTuple.getT2();
        if(!isMainOrganizationAuthenticated(principal)) {
            return Mono.just(emptyAuthAndMarkerRedirector(exchange));
        }
        return Mono.just(exchange);
    }

    private ServerWebExchange emptyAuthAndMarkerRedirector(ServerWebExchange exchange){
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if(response.getStatusCode() != HttpStatus.MOVED_PERMANENTLY) {
            return exchange.mutate().response(response).request(urlMutator(response, request, baseOperationUrlPath)).build();
        }
        return exchange;
    }

    private Mono<Tuple2<Principal, ServerWebExchange>> nextLevelMarkerRedirector(Principal principal, ServerWebExchange exchange){
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        if(response.getStatusCode() != HttpStatus.MOVED_PERMANENTLY) {
            Optional<ResponseCookie> cookie = cookieHelper.getCookieForAttribute(request.getHeaders().toSingleValueMap(), CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY, "Cookie");
            return markerContainerUtil.nextLevelMarker(cookie.orElseGet(cookieHelper.getDefaultCookie()).getValue())
                    .map(state -> Tuples.of(principal, exchange.mutate().response(response).request(tryUrlMutator(response, request, state)).build()));
        }
        return Mono.just(Tuples.of(principal, exchange));
    }

    private ServerHttpRequest tryUrlMutator(ServerHttpResponse response, ServerHttpRequest request, AuthenticatedResponse.Level state){
        String resolvedStateUrl = resolveMarkerState(state);
        if(resolvedStateUrl.equalsIgnoreCase(request.getPath().pathWithinApplication().value())){
            response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            return request;
        }
        return urlMutator(response, request, "/marker/"+state.name()+"/helper");
    }
    private ServerHttpRequest urlMutator(ServerHttpResponse response, ServerHttpRequest request, String path){
        ServerHttpRequest newRequest = request.mutate().contextPath("/").uri(getUri(request, path).orElse(null))
                .sslInfo(request.getSslInfo())
                .method(HttpMethod.GET)
                .path(path)
                .headers(e -> request.getHeaders())
                .remoteAddress(request.getRemoteAddress())
                .build();
        response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        response.getHeaders().set(HttpHeaders.LOCATION, path);
        return newRequest;
    }

    private Optional<URI> getUri(ServerHttpRequest request, String path) {
        URI originalUri = request.getURI();
        Optional<URI> url = Optional.empty();
        try {
            url = Optional.of(new URI(originalUri.getScheme(), originalUri.getUserInfo(), originalUri.getHost(), originalUri.getPort(), path, "", ""));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String getClientRegistrationId(Principal principal){
        if(principal instanceof OAuth2AuthenticationToken){
            return ((OAuth2AuthenticationToken) principal).getAuthorizedClientRegistrationId();
        }
        return "";
    }

    private Supplier<Principal> defaultPrincipal(){
        return () -> (Principal) () -> "__default__";
    }

    private boolean isMainOrganizationAuthenticated(Principal principal){
        return principal != null && MarkerContainerUtil.PRINCIPAL_AUTH_MARKER_SERVER.equalsIgnoreCase(getClientRegistrationId(principal));
    }

    //use cache regex
    private String resolveMarkerState(AuthenticatedResponse.Level value){
        String prefix = "/marker/", filter = "ORGANIZATION_", subject = value.name();
        return prefix + subject.substring(index(subject.indexOf(filter), filter) ).toLowerCase();
    }

    private int index(int subjectIndex, String filter){
        if (subjectIndex >= 0){
            return subjectIndex + filter.length();
        }
        return subjectIndex;
    }

    private Mono<Boolean> bypassMarkerCheckCondition(final GlobalMarker marker, final ServerWebExchange webExchange){
        return Mono.just(webExchange)
                .flatMap(exchange -> customWhiteListMatcher(marker, exchange.getRequest().getURI()))
                .flatMap(result -> defaultWhiteListMatcher(webExchange , result));
    }

    private Mono<Boolean> customWhiteListMatcher(GlobalMarker marker, URI uri){
        return Mono.just(uri).map(exchangeUrl -> {
            return StringUtils.hasText(marker.getOrganizationName()) ||
                    exchangeUrl.toString().contains(RouteUtil.Internal.ENDPOINT_LOGIN_ERROR) ||
                    exchangeUrl.toString().contains("/actuator") ||
                    exchangeUrl.getPath().startsWith("/auth") ||
                    exchangeUrl.getPath().isBlank() ||
                    exchangeUrl.getPath().equals("/");
        });
    }

    private Mono<Boolean> defaultWhiteListMatcher(ServerWebExchange exchange, Boolean prev){
        if(prev)
            return Mono.just(true);
        return new OrServerWebExchangeMatcher(
                ServerWebExchangeMatchers.pathMatchers(WHITELIST_RESOURCE_PATHS),
                ServerWebExchangeMatchers.pathMatchers(WHITELIST_URL_PATHS)
        ).matches(exchange).map(ServerWebExchangeMatcher.MatchResult::isMatch);
    }
}
