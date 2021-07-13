package com.board.wars.repository.custom;

import com.board.wars.domain.SubTask;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomSubTaskRepository {

    Mono<BaseMultiInterface.BaseSubTask<SubTask>> insertNewSubTask(Long code, String columnName, String taskName, SubTask subTask);

    Mono<BaseSingleInterface.BaseSubTask<SubTask>> updateSubTask(Long code, String columnName, String taskName, SubTask subTask);

    Mono<BaseMultiInterface.BaseSubTask<SubTask>> deleteSubTask(Long code, String columnName, String taskName, Long subTaskId);

    Mono<BaseSingleInterface.BaseSubTask<SubTask>> findByProjectColumnTask(Long code, String columnName, String taskName, Long subTaskId, Pageable pageable);

    Mono<BaseMultiInterface.BaseSubTask<SubTask>> findByProject(Long code, String columnName, String taskName, Pageable pageable);
}
