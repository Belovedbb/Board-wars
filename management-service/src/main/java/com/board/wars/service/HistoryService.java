package com.board.wars.service;

import com.board.wars.History;
import reactor.core.publisher.Flux;

public interface HistoryService {
    Flux<History> getHistories(Integer page, Integer size);
}
