package com.board.wars.repository.custom;

import com.board.wars.domain.CumulativeFlowDiagram;
import com.board.wars.domain.FlowDiagram;
import com.board.wars.repository.base.BaseSingleInterface;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public class CustomCumulativeFlowDiagramRepositoryImpl implements CustomCumulativeFlowDiagramRepository {

    private final ReactiveMongoTemplate template;

    public CustomCumulativeFlowDiagramRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<CumulativeFlowDiagram> insertNewFlowDiagramToCumulativeFlowDiagram(Long projectCode, FlowDiagram flowDiagram) {
        Query query = new Query(Criteria.where("projectCode").is(projectCode));//query.with()
        Update update = new Update().addToSet("flows", flowDiagram);
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), CumulativeFlowDiagram.class);
    }


    @Override
    public Mono<CumulativeFlowDiagram> findByFlowDiagram(Long projectCode, Pageable pageable) {
        Query query = new Query(Criteria.where("projectCode").is(projectCode)).with(pageable);
        query.fields().include("flows.$");
        return template.findOne(query, CumulativeFlowDiagram.class);
    }

    public Mono<CumulativeFlowDiagram> findByFlowDiagram(Long projectCode, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Query query = new Query(Criteria.where("projectCode").is(projectCode)
                .andOperator(
                        Criteria.where("flows.currentTime").lt(endDate),
                        Criteria.where("flows.currentTime").gte(startDate)
                )).with(pageable);
        query.fields().include("flows.$");
        return template.findOne(query, CumulativeFlowDiagram.class);
    }

}
