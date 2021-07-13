package com.board.wars.controller;

import com.board.wars.History;
import com.board.wars.service.HistoryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

@RestController
public class HistoryController {

    final private HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping(path = "/{auth}/kanban/history/{type}")
    public Flux<History> getPersistentKanbanHistories(String auth, @PathVariable String type,
                                                      @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return this.historyService.getHistories(type, page, size);
    }

    @GetMapping(path = "/kanban/history/{type}")
    public Flux<History> getApiPersistentKanbanHistories( @PathVariable String type, @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return this.historyService.getHistories(type, page, size);
    }
}
