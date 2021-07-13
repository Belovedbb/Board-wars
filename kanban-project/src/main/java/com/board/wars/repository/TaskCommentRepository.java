package com.board.wars.repository;

import com.board.wars.domain.Column;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.board.wars.repository.custom.CustomTaskCommentRepository;
import org.springframework.data.mongodb.repository.ExistsQuery;
import reactor.core.publisher.Mono;

public interface TaskCommentRepository extends ProjectRepository, BaseSingleInterface<Column>,
        BaseMultiInterface<Column>, CustomTaskCommentRepository {

    @ExistsQuery("{ 'columns.tasks.comments.code': ?0}")
    Mono<Boolean> taskCommentCodeIdExist(Long code);

}
