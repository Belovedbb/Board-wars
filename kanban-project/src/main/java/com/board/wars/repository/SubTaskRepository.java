package com.board.wars.repository;

import com.board.wars.domain.Column;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.board.wars.repository.custom.CustomSubTaskRepository;
import org.springframework.data.mongodb.repository.ExistsQuery;
import reactor.core.publisher.Mono;

public interface SubTaskRepository extends ProjectRepository, BaseSingleInterface<Column>,
        BaseMultiInterface<Column>, CustomSubTaskRepository {

    @ExistsQuery("{ 'columns.tasks.subTasks.code': ?0}")
    Mono<Boolean> subTaskCodeIdExist(Long code);
}
