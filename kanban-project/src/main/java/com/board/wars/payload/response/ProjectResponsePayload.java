package com.board.wars.payload.response;

import com.board.wars.payload.TeamUserPayload;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectResponsePayload {
    private String name;
    private long code;
    private String description;
    private LocalDateTime startPeriod;
    private LocalDateTime endPeriod;
    private List<String> tags;
    private TeamUserPayload teamUser;
    private String status;
    private CollectionModel<EntityModel<ColumnResponsePayload>> columns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public CollectionModel<EntityModel<ColumnResponsePayload>> getColumns() {
        return columns;
    }

    public void setColumns(CollectionModel<EntityModel<ColumnResponsePayload>> columns) {
        this.columns = columns;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTeamUser(TeamUserPayload teamUser) {
        this.teamUser = teamUser;
    }

    public LocalDateTime getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(LocalDateTime startPeriod) {
        this.startPeriod = startPeriod;
    }

    public LocalDateTime getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(LocalDateTime endPeriod) {
        this.endPeriod = endPeriod;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public TeamUserPayload getTeamUser() {
        return teamUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
