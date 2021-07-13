package com.board.wars.store.marker.parts;

import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import com.board.wars.domain.marker.parts.RoleMarkerPart;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RoleMarkerPartRepository extends ReactiveMongoRepository<RoleMarkerPart, String> {
    Mono<RoleMarkerPart> findByPartIdAndCompleteFalse(String part);
    Mono<RoleMarkerPart> findByPartIdAndCompleteTrue(String part);
    Mono<RoleMarkerPart> findByPartId(String part);
}
