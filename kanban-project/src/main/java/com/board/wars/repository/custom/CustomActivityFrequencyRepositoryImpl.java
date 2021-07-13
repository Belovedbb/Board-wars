package com.board.wars.repository.custom;

import com.board.wars.domain.ActivityFrequencyData;
import com.board.wars.domain.ActivityFrequencyDataPoint;
import com.board.wars.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class CustomActivityFrequencyRepositoryImpl implements CustomActivityFrequencyRepository {

    private final ReactiveMongoTemplate template;

    public CustomActivityFrequencyRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<ActivityFrequencyData> insertDataPoint(ActivityFrequencyData data, ActivityFrequencyDataPoint point) {
        Query query = new Query(where("_id").is(data.getId()));
        Update update = new Update().push("months", point);
        return template.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), ActivityFrequencyData.class);
    }
}
