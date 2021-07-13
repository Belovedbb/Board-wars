package com.board.wars.payload.response;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

public class ColumnResponsePayload {
    private String name;
    private String description;
    private String color;
    private short taskLimit;
    private CollectionModel<EntityModel<TaskResponsePayload>> tasks;

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

    public CollectionModel<EntityModel<TaskResponsePayload>> getTasks() {
        return tasks;
    }

    public void setTasks(CollectionModel<EntityModel<TaskResponsePayload>> tasks) {
        this.tasks = tasks;
    }
}
