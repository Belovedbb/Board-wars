package com.board.wars.store.marker;

import com.board.wars.domain.marker.global.GlobalMarker;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface GlobalMarkerRepository extends ReactiveMongoRepository<GlobalMarker, String> {
    Mono<GlobalMarker> findByOrganizationName(String name);
    Mono<GlobalMarker> findByApplicationName(String name);
    Mono<GlobalMarker> findByEmail(String name);

}
