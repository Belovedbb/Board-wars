package com.board.wars.repository;

import com.board.wars.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
    @Query("{'active' : ?0}")
    Flux<User> findByActiveStatus(boolean isActive, Pageable pageable);
    @Query("{ '_id': { $ne : null } } ")
    Flux<User> findAll(Pageable pageable);
    Mono<Long> countByUsernameInAndExpired(List<String> userNames, boolean expired);
    Flux<User> findUsersByUsernameInAndExpired(List<String> userNames, boolean expired);
}
