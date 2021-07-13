package com.board.wars.repository.custom;

import com.board.wars.domain.Team;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

public interface CustomTeamRepository {

    Flux<Team> getMemberRelatedTeams(String username, Pageable pageable);

    Flux<Team> getMemberRelatedTeamsByActive(String username, boolean active, Pageable pageable);
}
