package com.board.wars.payload.request;

public class TeamRequestPayload {
    private UserRequestPayload[] members;
    private String name;
    private String colorCode;
    private boolean active;
    private UserRequestPayload leader;

    public UserRequestPayload[] getMembers() {
        return members;
    }

    public void setMembers(UserRequestPayload[] members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UserRequestPayload getLeader() {
        return leader;
    }

    public void setLeader(UserRequestPayload leader) {
        this.leader = leader;
    }
}
