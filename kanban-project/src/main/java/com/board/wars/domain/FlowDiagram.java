package com.board.wars.domain;

import java.time.LocalDate;

public class FlowDiagram {

    private Column column;
    private LocalDate currentTime;

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public LocalDate getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDate currentTime) {
        this.currentTime = currentTime;
    }
}
