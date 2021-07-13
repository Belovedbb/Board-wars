package com.board.wars.payload.request;

import com.board.wars.payload.TeamUserPayload;

import java.time.LocalDateTime;
import java.util.List;

public class TaskRequestPayload {
    //name must be unique
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TeamUserPayload assignee;
    private List<SubTaskRequestPayload> subTasks;
    private List<TaskCommentRequestPayload> comments;
    private List<String> categories;
    private List<String> tags;

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

    public TeamUserPayload getAssignee() {
        return assignee;
    }

    public void setAssignee(TeamUserPayload assignee) {
        this.assignee = assignee;
    }

    public List<SubTaskRequestPayload> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTaskRequestPayload> subTasks) {
        this.subTasks = subTasks;
    }

    public List<TaskCommentRequestPayload> getComments() {
        return comments;
    }

    public void setComments(List<TaskCommentRequestPayload> comments) {
        this.comments = comments;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
