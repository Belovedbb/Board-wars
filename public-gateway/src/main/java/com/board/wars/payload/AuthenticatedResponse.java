package com.board.wars.payload;

import com.board.wars.payload.marker.MarkerResponse;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDateTime;

public class AuthenticatedResponse{
    public static final String AUTHENTICATED_ORGANIZATION_MESSAGE = "access denied, please consider registering an organization for the application";
    public static final String SUCCESSFUL_AUTHENTICATED_ORGANIZATION_MESSAGE = "board-wars application has been successfully configured ";
    public enum  Level{
        PRIMARY,
        PRIMARY_SUCCESS,
        PRIMARY_FAILURE,
        ORGANIZATION_GLOBAL,
        ORGANIZATION_STORAGE,
        ORGANIZATION_ROLE,
        ORGANIZATION_TOKEN,
        ORGANIZATION_SUCCESS,
        ORGANIZATION_FAILURE,
        INTERNAL
    }
    private boolean status;
    private String value;
    private Level level;
    private LocalDateTime period;
    private Object additionalInfo;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public LocalDateTime getPeriod() {
        return period;
    }

    public void setPeriod(LocalDateTime period) {
        this.period = period;
    }

    public Object getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public AuthenticatedResponse(){
        this(false, "", Level.PRIMARY, LocalDateTime.now(Clock.systemUTC()));
    }

    public AuthenticatedResponse(boolean status, String value, Level level, LocalDateTime period) {
        this(status, value, level, period, null);
    }

    public AuthenticatedResponse(boolean status, String value, Level level, LocalDateTime period, Object additionalInfo) {
        this.status = status;
        this.value = value;
        this.level = level;
        this.period = period;
        this.additionalInfo = additionalInfo;
    }

    //helper
    public static AuthenticatedResponse populateResponse(AuthenticatedResponse.Level level, boolean status){
        AuthenticatedResponse response = new AuthenticatedResponse();
        switch (level){
            case ORGANIZATION_TOKEN:
                response = new AuthenticatedResponse(status, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.ORGANIZATION_TOKEN, LocalDateTime.now());
                break;
            case ORGANIZATION_GLOBAL:
                response = new AuthenticatedResponse(status, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.ORGANIZATION_GLOBAL, LocalDateTime.now());
                break;
            case ORGANIZATION_ROLE:
                response = new AuthenticatedResponse(status, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.ORGANIZATION_ROLE, LocalDateTime.now());
                break;
            case ORGANIZATION_STORAGE:
                response = new AuthenticatedResponse(status, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.ORGANIZATION_STORAGE, LocalDateTime.now());
                break;
        }
        return response;
    }

}
