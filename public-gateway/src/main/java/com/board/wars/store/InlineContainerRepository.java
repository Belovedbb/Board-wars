package com.board.wars.store;

import com.board.wars.domain.InlineContainer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface InlineContainerRepository extends ReactiveMongoRepository<InlineContainer, String> {
    Mono<Void> deleteInlineContainersByContainerId(String container);
    Mono<InlineContainer> getInlineContainerByContainerId(String containerId);
    Flux<Void> deleteAllByValidUntilBefore(LocalDateTime factor);
}
