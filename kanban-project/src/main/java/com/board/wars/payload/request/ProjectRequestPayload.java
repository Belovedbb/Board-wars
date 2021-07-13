package com.board.wars.payload.request;

import com.board.wars.payload.TeamUserPayload;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectRequestPayload {
    private String description;
    private String name;
    private String status;
    private LocalDateTime endPeriod;
    private TeamUserPayload teamUser;
    private List<String> tags;
    private List<ColumnRequestPayload> columns;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(LocalDateTime endPeriod) {
        this.endPeriod = endPeriod;
    }

    public TeamUserPayload getTeamUser() {
        return teamUser;
    }

    public void setTeamUser(TeamUserPayload teamUser) {
        this.teamUser = teamUser;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<ColumnRequestPayload> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnRequestPayload> columns) {
        this.columns = columns;
    }
}
