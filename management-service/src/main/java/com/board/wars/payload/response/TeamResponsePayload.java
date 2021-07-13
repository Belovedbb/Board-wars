package com.board.wars.payload.response;

import java.time.LocalDate;

public class TeamResponsePayload {
    private UserResponsePayload[] members;
    private String name;
    private String code;
    private String colorCode;
    private boolean isActive;
    private UserResponsePayload leader;
    private String[] roles;
    private LocalDate dateCreated;

    public UserResponsePayload[] getMembers() {
        return members;
    }

    public void setMembers(UserResponsePayload[] members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public UserResponsePayload getLeader() {
        return leader;
    }

    public void setLeader(UserResponsePayload leader) {
        this.leader = leader;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }
}
