package com.board.wars.payload.request;

import java.util.List;

public class ColumnRequestPayload {
    private String name;
    private String description;
    private String color;
    private short taskLimit;
    private List<TaskRequestPayload> tasks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public short getTaskLimit() {
        return taskLimit;
    }

    public void setTaskLimit(short taskLimit) {
        this.taskLimit = taskLimit;
    }

    public List<TaskRequestPayload> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskRequestPayload> tasks) {
        this.tasks = tasks;
    }
}
