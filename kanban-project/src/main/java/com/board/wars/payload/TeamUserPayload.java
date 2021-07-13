package com.board.wars.payload;

public class TeamUserPayload {

    private String identity;
    private TeamUserPayload.Type type;


    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type{
        USER,
        TEAM
    }
}
