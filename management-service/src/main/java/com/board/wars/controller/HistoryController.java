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

    @GetMapping(value = "/{auth}/management/history")
    public Flux<History> getPersistentManagementHistories(@PathVariable String auth,  @RequestParam(required = false) Integer page,
                                                      @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return this.historyService.getHistories(page, size);
    }

    @GetMapping(value = "/management/history")
    public Flux<History> getApiPersistentManagementHistories( @RequestParam(required = false) Integer page,
                                                           @RequestParam(required = false) Integer size, ServerWebExchange exchange){
        return this.historyService.getHistories(page, size);
    }
}
