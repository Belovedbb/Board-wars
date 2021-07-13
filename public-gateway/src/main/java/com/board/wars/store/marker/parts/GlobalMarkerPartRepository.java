package com.board.wars.store.marker.parts;

import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface GlobalMarkerPartRepository extends ReactiveMongoRepository<GlobalMarkerPart, String> {
    Mono<GlobalMarkerPart> findByPartIdAndCompleteFalse(String part);
    Mono<GlobalMarkerPart> findByPartIdAndCompleteTrue(String part);
    Mono<GlobalMarkerPart> findByPartId(String part);
}
