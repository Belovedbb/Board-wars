package com.board.wars.repository;

import com.board.wars.domain.Task;
import com.board.wars.repository.base.BaseMultiInterface;
import com.board.wars.repository.base.BaseSingleInterface;
import com.board.wars.repository.custom.CustomTaskRepository;

public interface TaskRepository extends ProjectRepository, BaseMultiInterface<Task>, BaseSingleInterface<Task>, CustomTaskRepository {

}
