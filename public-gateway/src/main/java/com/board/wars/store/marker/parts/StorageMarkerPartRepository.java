package com.board.wars.store.marker.parts;

import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import com.board.wars.domain.marker.parts.StorageMarkerPart;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface StorageMarkerPartRepository extends ReactiveMongoRepository<StorageMarkerPart, String> {
    Mono<StorageMarkerPart> findByPartIdAndCompleteFalse(String part);
    Mono<StorageMarkerPart> findByPartIdAndCompleteTrue(String part);
    Mono<StorageMarkerPart> findByPartId(String part);
}
