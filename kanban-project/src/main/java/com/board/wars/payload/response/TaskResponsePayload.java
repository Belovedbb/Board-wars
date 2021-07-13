package com.board.wars.payload.response;

import com.board.wars.payload.TeamUserPayload;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.time.LocalDateTime;
import java.util.List;

public class TaskResponsePayload {
    private Long position;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> categories;
    private List<String> tags;
    private TeamUserPayload assignee;
    private TeamUserPayload reporter;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private CollectionModel<EntityModel<SubTaskResponsePayload>> subTasks;
    private CollectionModel<EntityModel<TaskCommentResponsePayload>> comments;

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

    public CollectionModel<EntityModel<SubTaskResponsePayload>> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(CollectionModel<EntityModel<SubTaskResponsePayload>> subTasks) {
        this.subTasks = subTasks;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public CollectionModel<EntityModel<TaskCommentResponsePayload>> getComments() {
        return comments;
    }

    public void setComments(CollectionModel<EntityModel<TaskCommentResponsePayload>> comments) {
        this.comments = comments;
    }

    public TeamUserPayload getAssignee() {
        return assignee;
    }

    public void setAssignee(TeamUserPayload assignee) {
        this.assignee = assignee;
    }

    public TeamUserPayload getReporter() {
        return reporter;
    }

    public void setReporter(TeamUserPayload reporter) {
        this.reporter = reporter;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
