package com.board.wars.repository;

import com.board.wars.domain.HistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface HistoryRepository extends ReactiveMongoRepository<HistoryEntity, String> {
    @Query("{ '_id': { $ne : null } } ")
    Flux<HistoryEntity> findAll(Pageable pageable);
    Flux<HistoryEntity> findAllByType(String type, Pageable pageable);
}
