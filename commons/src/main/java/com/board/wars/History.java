package com.board.wars;

import java.time.LocalDateTime;
import java.util.UUID;

public class History {

    private String id;
    private String title;
    private String message;
    private LocalDateTime eventPeriod;
    private HistoryCategory category;
    private String username;
    private String type;
    private Object data;

    public History() {
        this.id = UUID.randomUUID().toString();
        this.category = HistoryCategory.SUCCESS;
        this.data = null;
        this.type = "";
        this.eventPeriod = LocalDateTime.now();
    }

    public History(String id, String title, String message, LocalDateTime eventPeriod, String type,
                   HistoryCategory category, String username, Object data) {
        this.title = title;
        this.id = id;
        this.message = message;
        this.eventPeriod = eventPeriod;
        this.category = category;
        this.username = username;
        this.data = data;
        this.type = type;
    }

    public History(String message, String title, LocalDateTime eventPeriod, String type, String username, Object data) {
        this.title = title;
        this.message = message;
        this.eventPeriod = eventPeriod;
        this.id = UUID.randomUUID().toString();
        this.category = HistoryCategory.SUCCESS;
        this.username = username;
        this.data = data;
        this.type = type;
    }

    public History(String message, String title, LocalDateTime eventPeriod, String username) {
        this.title = title;
        this.message = message;
        this.eventPeriod = eventPeriod;
        this.id = UUID.randomUUID().toString();
        this.category = HistoryCategory.SUCCESS;
        this.username = username;
        this.type = "";
        this.data = null;
    }

    public History(String message, String title, String type,  String username) {
        this.title = title;
        this.message = message;
        this.eventPeriod = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
        this.category = HistoryCategory.SUCCESS;
        this.username = username;
        this.type = type;
        this.data = null;
    }

    public History(String message, String title, String username) {
        this.title = title;
        this.message = message;
        this.eventPeriod = LocalDateTime.now();
        this.id = UUID.randomUUID().toString();
        this.category = HistoryCategory.SUCCESS;
        this.username = username;
        this.type = "";
        this.data = null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getEventPeriod() {
        return eventPeriod;
    }

    public void setEventPeriod(LocalDateTime eventPeriod) {
        this.eventPeriod = eventPeriod;
    }

    public HistoryCategory getCategory() {
        return category;
    }

    public void setCategory(HistoryCategory category) {
        this.category = category;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
