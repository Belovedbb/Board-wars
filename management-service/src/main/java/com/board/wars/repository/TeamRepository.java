package com.board.wars.repository;

import com.board.wars.domain.Team;
import com.board.wars.repository.custom.CustomTeamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeamRepository extends ReactiveMongoRepository<Team, String>, CustomTeamRepository {
    @Query("{'code' : ?0}")
    Mono<Team> findByCode(String code);
    @Query("{'active' : ?0}")
    Flux<Team> findByActiveStatus(boolean isActive, Pageable pageable);
    Flux<Team> findByMembersContaining(boolean isActive, Pageable pageable);
    @Query("{ '_id': { $ne : null } } ")
    Flux<Team> findAll(Pageable pageable);
}