package com.board.wars.repository.base;

import java.util.HashSet;
import java.util.List;

public interface BaseMultiInterface<T> {

    interface BaseTask<T> {
        List<T> getTasks();
    }

    interface BaseColumn<T> {
        List<T> getColumns();
    }

    interface BaseSubTask<T> {
        List<T> getSubTasks();
    }
    interface BaseTaskComment<T> {
        List<T> getTaskComments();
    }

    interface BaseCumulativeFlowDiagram<T> {
        List<T> getCumulativeFlowDiagram();
    }
}
