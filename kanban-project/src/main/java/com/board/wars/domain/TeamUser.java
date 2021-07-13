package com.board.wars.domain;

public class TeamUser {
    private String id;

    private String name;

    private MemberType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MemberType getType() {
        return type;
    }

    public void setType(MemberType type) {
        this.type = type;
    }

    public enum MemberType {
        USER,
        TEAM
    }
}
