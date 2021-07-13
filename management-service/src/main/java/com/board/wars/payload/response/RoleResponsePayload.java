package com.board.wars.payload.response;

public class RoleResponsePayload {
    private String name;
    private UserResponsePayload[] users;
    private TeamResponsePayload[] teams;
    private boolean isCustom;
    private short precedence;
}
