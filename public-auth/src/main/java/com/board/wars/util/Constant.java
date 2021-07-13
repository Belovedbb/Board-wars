package com.board.wars.util;

public class Constant {
    public  enum ROLE{
        VISITOR,
        ADMIN,
        LEADER,

    }

    public static class RoleConverter{
        String convertToValue_(ROLE role){
            return role.name();
        }

        ROLE convertToRole_(String value){
            return ROLE.valueOf(value);
        }
    }

    public static final String REDIRECT_KEY = "X-LOCAL-AUTH-REDIRECT";
    public static final String SUCCESSFUL_STATE_MESSAGE = "the operation was successfully received and established";
    public static final String FAILURE_STATE_MESSAGE = "the operation failed";
}
