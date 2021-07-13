package com.board.wars.historize;

import com.board.wars.History;
import com.board.wars.historize.kanban.controller.KanbanEventController;
import com.board.wars.historize.management.controller.ManagementEventController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "/{auth}/history")
public class HistorizeController {
    private final Sinks.Many<History> historizeSink;
    @Autowired
    KanbanEventController kanbanController;
    @Autowired
    ManagementEventController managementEventController;

    public HistorizeController(Sinks.Many<History> historizeSink ) {
        this.historizeSink = historizeSink;
    }

    @GetMapping(path = "/events/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<History>> getAllEvents() {
        return historizeSink.asFlux().map(data -> ServerSentEvent.builder(data).id(UUID.randomUUID().toString()).build());
    }

    @GetMapping(path = "/persistent/all")
    public Flux<History> getHistories( @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return this.kanbanController.getPersistentKanbanHistories("all", page, size, exchange)
                .concatWith(managementEventController.getPersistentManagementHistories(page, size, exchange))
                .sort(getComparator().get());
    }

    private  Supplier<Comparator<History>> getComparator() {
        return new Supplier<Comparator<History>>() {
            @Override
            public Comparator<History> get() {
                return new Comparator<History>() {
                    @Override
                    public int compare(History o1, History o2) {
                        if( o1.getEventPeriod().isBefore(o2.getEventPeriod())) return 1;
                        else if(o1.getEventPeriod().isAfter(o2.getEventPeriod())) return -1;
                        else return 0;
                    }
                };
            }
        };
    }

}
