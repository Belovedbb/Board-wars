package com.board.wars.config.route;

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
public class MainRouter {

    final private RouteLocatorBuilder routeBuilder;
    final private TokenRelayGatewayFilterFactory filterFactory;

    MainRouter(RouteLocatorBuilder routeBuilder, TokenRelayGatewayFilterFactory filterFactory){
        this.routeBuilder = routeBuilder;
        this.filterFactory = filterFactory;
    }

    @Bean
    RouteLocator locator(){
        return routeBuilder.routes()
                .route(this::kanbanV1Route)
                .route(this::scrumV1Route)
                .route(this::mixedV1Route)
                .route(this::managementV1Route)
                .route(this::historyV1Route)
                .build();
    }

    private Buildable<Route> kanbanV1Route(PredicateSpec spec){
        return spec.path("/*/kanban/**")
                .filters(
                        filter -> filter
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(filterFactory.apply())
                )
                .uri("lb://kanban-project");
    }

    private Buildable<Route> scrumV1Route(PredicateSpec spec){
        return spec.path("/*/scrum/**")
                .filters(
                        filter -> filter
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(filterFactory.apply())
                )
                .uri("lb://scrum-project");
    }

    private Buildable<Route> mixedV1Route(PredicateSpec spec){
        return spec.path("/*/mixed/**")
                .filters(
                        filter -> filter
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(filterFactory.apply())
                )
                .uri("lb://mixed-project");
    }

    private Buildable<Route> managementV1Route(PredicateSpec spec){
        return spec.path("/*/management/**")
                .filters(
                        filter -> filter
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(filterFactory.apply())
                )
                .uri("lb://management-service");
    }

    private Buildable<Route> historyV1Route(PredicateSpec spec){
        return spec.path("/*/history/**")
                .filters(
                        filter -> filter
                                .removeRequestHeader(HttpHeaders.COOKIE)
                                .removeRequestHeader(HttpHeaders.SET_COOKIE)
                                .filter(filterFactory.apply())
                )
                .uri("lb://v1-delegator");
    }
}
