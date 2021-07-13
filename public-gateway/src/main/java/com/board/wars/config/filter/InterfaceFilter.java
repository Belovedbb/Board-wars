package com.board.wars.config.filter;

import com.board.wars.config.auth.properties.ApplicationProperties;
import com.board.wars.utils.RouteUtil;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class InterfaceFilter implements WebFilter {

    private final ServerRequestCache requestCache;
    private final ApplicationProperties appProperties;
    private final ResourceLoader resourceLoader;

    public InterfaceFilter(ServerRequestCache requestCache, ApplicationProperties appProperties, ResourceLoader resourceLoader) {
        this.requestCache = requestCache;
        this.appProperties = appProperties;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        //TODO improve conditionals by using ServerWebExchangeMatchers.pathMatchers
        if (exchange.getRequest().getURI().getPath().equals("/")) {
            return requestCache.getRedirectUri(exchange)
                    .defaultIfEmpty(URI.create(""))
                    .flatMap(uri -> this.resolveDestination(exchange, chain, uri, URI.create("")));
        }else if (exchange.getRequest().getURI().getPath().startsWith("/pages/")){
            return redirectInternalRoute(exchange, chain);
        }
        return chain.filter(exchange);
    }

    private Mono<Void> resolveDestination(ServerWebExchange exchange, WebFilterChain chain, URI uri, URI defaultUri){
        if(uri.getPath().equals(defaultUri.getPath())){
            return redirectInternalRoute(exchange, chain);
        }else{
            return requestCache.removeMatchingRequest(exchange)
                    .map(req -> appProperties.getInterfaceHost())
                    .flatMap(host -> redirectExternalRoute(exchange, chain, host));
        }
    }

    private Mono<Void> redirectExternalRoute(ServerWebExchange exchange, WebFilterChain chain, String host){
        return StringUtils.hasText(host) ?
                RouteUtil.redirect(exchange.getResponse(), URI.create(host)) :
                chain.filter(exchange);
    }

    private Mono<Void> redirectInternalRoute(ServerWebExchange exchange, WebFilterChain chain){
        return resourceLoader.getResource("classpath:/public/index.html").exists() ?
                chain.filter(exchange.mutate().request(exchange.getRequest().mutate().path("/index.html").build()).build()) :
                chain.filter(exchange);
    }
}

