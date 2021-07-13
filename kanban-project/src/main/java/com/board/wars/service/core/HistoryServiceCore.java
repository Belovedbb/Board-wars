package com.board.wars.service.core;

import com.board.wars.History;
import com.board.wars.domain.HistoryEntity;
import com.board.wars.repository.HistoryRepository;
import com.board.wars.service.HistoryService;
import com.board.wars.util.Utilities;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.board.wars.util.Utilities.KANBAN;

@Service
public class HistoryServiceCore implements HistoryService {
    private final static String ALL = "all";
    private final static String DOMAIN = "domain";
    private final static String GRAPHICS = "graphics";

    final private HistoryRepository historyRepository;

    public HistoryServiceCore(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    @Bulkhead(name = KANBAN)
    @CircuitBreaker(name = KANBAN)
    @TimeLimiter(name = KANBAN)
    public Flux<History> getHistories(String type, Integer page, Integer size) {
        return delegator(type, page, size);
    }

    private Flux<History> delegator(String type,  Integer page, Integer size) {
        if(page == null || size == null) {
            page = 0; size = 10;
        }
        switch (type) {
            case ALL: return this.getAllType(page, size);
            case DOMAIN: return this.getDomainType(page, size);
            case GRAPHICS: return this.getGraphicsType(page, size);
        }
        return Flux.empty();
    }

    private Flux<History> getAllType(Integer page, Integer size) {
        return  historyRepository
                .findAll(Utilities.Pager.resolvePagerFromSize(page, size, Sort.by(Sort.Order.desc("history.eventPeriod"))))
                .map(HistoryEntity::getHistory);
    }

    private Flux<History> getDomainType(Integer page, Integer size) {
        return  historyRepository
                .findAllByType(DOMAIN, Utilities.Pager.resolvePagerFromSize(page, size, Sort.by(Sort.Order.desc("history.eventPeriod"))))
                .map(HistoryEntity::getHistory);
    }

    private Flux<History> getGraphicsType(Integer page, Integer size) {
        return  historyRepository
                .findAllByType(GRAPHICS, Utilities.Pager.resolvePagerFromSize(page, size, Sort.by(Sort.Order.desc("history.eventPeriod"))))
                .map(HistoryEntity::getHistory);
    }

}
