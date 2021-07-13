package com.board.wars.repository;

import com.board.wars.domain.CumulativeFlowDiagram;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.custom.CustomCumulativeFlowDiagramRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CumulativeFlowDiagramRepository extends ReactiveMongoRepository<CumulativeFlowDiagram, String>,
        BaseMultiInterface<CumulativeFlowDiagram>, CustomCumulativeFlowDiagramRepository {
    Mono<CumulativeFlowDiagram> findByProjectCode(Long code);
    @Query("{ '_id': { $ne : null } } ")
    Flux<CumulativeFlowDiagram> findAll(Pageable pageable);
    @ExistsQuery("{ 'projectCode': ?0}")
    Mono<Boolean> projectCodeExist(Long code);


}
