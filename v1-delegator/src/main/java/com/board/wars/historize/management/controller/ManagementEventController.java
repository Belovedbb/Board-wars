package com.board.wars.historize.management.controller;

import com.board.wars.History;
import com.board.wars.config.properties.RouteProperties;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "/{auth}/history")
public class ManagementEventController {
    private final Sinks.Many<History> managementSink;
    final private WebClient webClient;
    final private RouteProperties routeProperties;

    public ManagementEventController(Sinks.Many<History> managementSink, WebClient webClient, RouteProperties routeProperties) {
        this.managementSink = managementSink;
        this.webClient = webClient;
        this.routeProperties = routeProperties;
    }

    @GetMapping(path = "/events/management", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<History>> getAllManagementEvents() {
        return managementSink.asFlux().map(data -> ServerSentEvent.builder(data).id(UUID.randomUUID().toString()).build());
    }

    //type can be -> all, domain, graphics
    @GetMapping(path = "/persistent/management")
    public Flux<History> getPersistentManagementHistories(@RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return webClient.get().uri(URI.create(Utilities.resolveRoute(routeProperties.getIntermediate().getManagementHistoryLink(), String.valueOf(page), String.valueOf(size))))
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, response -> Mono.just(new IllegalStateException("unable to get resources: "+ response.statusCode().name())))
                .bodyToFlux(History.class);
    }

}
