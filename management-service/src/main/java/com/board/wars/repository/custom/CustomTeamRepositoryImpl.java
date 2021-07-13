package com.board.wars.repository.custom;

import com.board.wars.domain.Team;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CustomTeamRepositoryImpl implements CustomTeamRepository {

    private final ReactiveMongoTemplate template;

    public CustomTeamRepositoryImpl(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    public Flux<Team> getMemberRelatedTeams(String username, Pageable pageable){
        Query query = new Query();
        query.addCriteria(Criteria.where("members.username").is(username));
        return template.find(query, Team.class);
    }

    @Override
    public Flux<Team> getMemberRelatedTeamsByActive(String username, boolean active, Pageable pageable){
        return getMemberRelatedTeams(username, pageable);
    }

}
