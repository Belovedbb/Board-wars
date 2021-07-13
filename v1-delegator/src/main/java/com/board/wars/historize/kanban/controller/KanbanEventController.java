package com.board.wars.historize.kanban.controller;

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
public class KanbanEventController {
    private final Sinks.Many<History> kanbanSink;
    private final Sinks.Many<History> projectSink;
    private final Sinks.Many<History> graphicsSink;
    final private WebClient webClient;
    final private RouteProperties routeProperties;

    public KanbanEventController(Sinks.Many<History> kanbanSink, Sinks.Many<History> projectSink, Sinks.Many<History> graphicsSink, WebClient webClient, RouteProperties routeProperties) {
        this.kanbanSink = kanbanSink;
        this.projectSink = projectSink;
        this.graphicsSink = graphicsSink;
        this.webClient = webClient;
        this.routeProperties = routeProperties;
    }

    @GetMapping(path = "/events/kanban", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<History>> getAllKanbanEvents() {
        return kanbanSink.asFlux().map(data -> ServerSentEvent.builder(data).id(UUID.randomUUID().toString()).build());
    }

    @GetMapping(path = "/events/kanban/project", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<History>> getKanbanProjectEvents() {
        return projectSink.asFlux().map(data -> ServerSentEvent.builder(data).id(UUID.randomUUID().toString()).build());
    }

    @GetMapping(path = "/events/kanban/graphics", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<History>> getAllKanbanGraphicsEvents() {
        return graphicsSink.asFlux().map(data -> ServerSentEvent.builder(data).id(UUID.randomUUID().toString()).build());
    }

    //type can be -> all, domain, graphics
    @GetMapping(path = "/persistent/kanban/{type}")
    public Flux<History> getPersistentKanbanHistories(@PathVariable String type, @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return webClient.get().uri(URI.create(Utilities.resolveRoute(routeProperties.getIntermediate().getKanbanHistoryLink(), type, String.valueOf(page), String.valueOf(size))))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, response -> Mono.just(new IllegalStateException("unable to get resources: "+ response.statusCode().name())))
                .bodyToFlux(History.class);
    }

}
