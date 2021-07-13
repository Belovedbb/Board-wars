package com.board.wars.repository.custom;


import com.board.wars.domain.Task;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface CustomTaskRepository {

    Mono<BaseMultiInterface.BaseTask<Task>> insertNewTaskToProject(Long code, String columnName, Task task);

    Mono<BaseSingleInterface.BaseTask<Task>> updateProjectTask(Long code, String columnName, Task task);

    Mono<BaseMultiInterface.BaseTask<Task>> deleteProjectTask(Long code, String columnName, String taskName);

    Mono<BaseSingleInterface.BaseTask<Task>> findByProjectColumnTaskName(Long code, String name, String taskName, Pageable pageable);

    Mono<BaseMultiInterface.BaseTask<Task>> findAllTaskFromProject(Long code, String columnName, Pageable pageable);

    Mono<BaseMultiInterface.BaseTask<Task>> updateProjectColumnTasks(Long projectCode, String columnName, Task[] tasks);

}
