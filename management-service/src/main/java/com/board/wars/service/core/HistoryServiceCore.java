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

import static com.board.wars.util.Utilities.MANAGEMENT;

@Service
public class HistoryServiceCore implements HistoryService {

    final private HistoryRepository historyRepository;

    public HistoryServiceCore(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Bulkhead(name = MANAGEMENT)
    @CircuitBreaker(name = MANAGEMENT)
    @TimeLimiter(name = MANAGEMENT)
    public Flux<History> getHistories(Integer page, Integer size) {
        return delegator( page, size);
    }

    private Flux<History> delegator( Integer page, Integer size) {
        if(page == null || size == null) {
            page = 0; size = 10;
        }
        return  historyRepository
                .findAll(Utilities.Pager.resolvePagerFromSize(page, size, Sort.by(Sort.Order.desc("history.eventPeriod"))))
                .map(HistoryEntity::getHistory);
    }
}
