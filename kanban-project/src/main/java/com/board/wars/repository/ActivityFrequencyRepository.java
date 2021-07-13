package com.board.wars.repository;

import com.board.wars.domain.ActivityFrequencyData;
import com.board.wars.repository.custom.CustomActivityFrequencyRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ActivityFrequencyRepository extends ReactiveMongoRepository<ActivityFrequencyData, String>,
        CustomActivityFrequencyRepository {
    Mono<ActivityFrequencyData> findByTitle(String title);
}
