package com.board.wars.domain;

import com.board.wars.History;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class HistoryEntity  {
    @Id
    private String id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
