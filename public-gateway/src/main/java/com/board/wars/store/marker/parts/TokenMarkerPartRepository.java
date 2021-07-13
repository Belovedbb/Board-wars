package com.board.wars.store.marker.parts;

import com.board.wars.domain.marker.parts.TokenMarkerPart;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TokenMarkerPartRepository extends ReactiveMongoRepository<TokenMarkerPart, String> {
    Mono<TokenMarkerPart> findByPartIdAndCompleteFalse(String part);
    Mono<TokenMarkerPart> findByPartIdAndCompleteTrue(String part);
    Mono<TokenMarkerPart> findByPartId(String part);
}
