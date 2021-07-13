package com.board.wars.repository;

import com.board.wars.domain.Project;
import com.board.wars.domain.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
    Mono<Project> findByCode(Long code);
    Flux<Project> findByStatus(Status status, Pageable pageable);
    @Query("{ '_id': { $ne : null } } ")
    Flux<Project> findAll(Pageable pageable);

    @ExistsQuery("{ 'code': ?0}")
    Mono<Boolean> projectCodeIdExist(Long code);
}
