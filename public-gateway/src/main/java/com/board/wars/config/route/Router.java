package com.board.wars.config.route;

import com.board.wars.config.filter.MarkerTokenGatewayFilter;
import com.board.wars.config.filter.RewritePathWithClientRegistrationGatewayFilter;
import com.board.wars.utils.RouteUtil;
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.Buildable;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
public class Router {

    private RouteLocatorBuilder routeBuilder;
    private TokenRelayGatewayFilterFactory tokenRelayFilterFactory;
    private RewritePathWithClientRegistrationGatewayFilter rewriteFilter;
    private final MarkerTokenGatewayFilter markerTokenFilter;
    final private String API_VERSION_KEY = "api_version_key";

    Router(RouteLocatorBuilder routeBuilder, TokenRelayGatewayFilterFactory tokenRelayFilterFactory, RewritePathWithClientRegistrationGatewayFilter rewriteFilter, MarkerTokenGatewayFilter markerTokenFilter){
        rewriteFilter.setRegexAndReplacement(RouteUtil.Internal.API_VERSION_ROUTE +"(?<path>.*)", "/$\\{path}");
        this.routeBuilder = routeBuilder;
        this.tokenRelayFilterFactory = tokenRelayFilterFactory;
        this.rewriteFilter = rewriteFilter;
        this.markerTokenFilter = markerTokenFilter;
    }

    @Bean
    RouteLocator route(){
        return routeBuilder.routes()
                .route(this::apiV1Route)
                .build();
    }

    private Buildable<Route> apiV1Route(PredicateSpec spec){
        return spec.path(RouteUtil.Internal.API_VERSION_ROUTE + "**")
                .filters(
                        filter -> filter
                                .addRequestHeader(API_VERSION_KEY, RouteUtil.Internal.API_VERSION_ROUTE)
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(tokenRelayFilterFactory.apply())
                                .filter(markerTokenFilter)
                                .filter(rewriteFilter)
                )
                .uri("lb://v1-delegator");
    }
}
