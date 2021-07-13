package com.board.wars.repository.custom;

import com.board.wars.domain.TaskComment;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomTaskCommentRepository {

    Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> insertNewTaskComment(Long code, String columnName, String taskName, TaskComment taskComment);

    Mono<BaseSingleInterface.BaseTaskComment<TaskComment>> updateTaskComment(Long code, String columnName, String taskName, TaskComment taskComment);

    Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> deleteTaskComment(Long code, String columnName, String taskName, Long taskCommentId);

    Mono<BaseSingleInterface.BaseTaskComment<TaskComment>> findByProjectColumnTask(Long code, String columnName, String taskName, Long taskCommentId, Pageable pageable);

    Mono<BaseMultiInterface.BaseTaskComment<TaskComment>> findByProject(Long code, String columnName, String taskName, Pageable pageable);

}
