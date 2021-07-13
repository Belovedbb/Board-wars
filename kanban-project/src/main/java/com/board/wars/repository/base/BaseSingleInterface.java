package com.board.wars.repository.base;

public interface BaseSingleInterface<T> {

    interface BaseColumn<T> {
        T getColumn();
    }

    interface BaseTask<T> {
        T getTask();
    }

    interface BaseSubTask<T> {
        T getSubTask();
    }

    interface BaseTaskComment<T> {
        T getTaskComment();
    }

    interface BaseCumulativeFlowDiagram<T> {
        T getCumulativeFlowDiagram();
    }

}
