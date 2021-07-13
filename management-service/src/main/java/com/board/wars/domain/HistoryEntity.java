package com.board.wars.domain;

import com.board.wars.History;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class HistoryEntity extends BaseEntity {
    @Indexed
    private String type;
    private History history;

    public HistoryEntity(String type, History history) {
        this.type = type;
        this.history = history;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }
}
