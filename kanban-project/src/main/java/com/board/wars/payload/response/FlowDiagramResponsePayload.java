package com.board.wars.payload.response;

import org.springframework.util.CollectionUtils;

import java.time.LocalDate;

public class FlowDiagramResponsePayload {
    private ColumnResponsePayload column;
    private LocalDate currentTime;
    private long taskSize;

    public ColumnResponsePayload getColumn() {
        return column;
    }

    public void setColumn(ColumnResponsePayload column) {
        this.column = column;
    }

    public LocalDate getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDate currentTime) {
        this.currentTime = currentTime;
    }

    public long getTaskSize() {
        return taskSize;
    }

    public void setTaskSize(long taskSize) {
        this.taskSize = taskSize;
    }
}
