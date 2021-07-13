package com.board.wars.domain.marker.parts;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;

@Document
public class TokenMarkerPart extends MarkerPart{
    @Id
    public String id;

    private boolean enableToken;

    private boolean enableGithubAuth;

    private Duration tokenLifeTime;

    private String baseEmail;

    private String attachedRole;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnableToken() {
        return enableToken;
    }

    public void setEnableToken(boolean enableToken) {
        this.enableToken = enableToken;
    }

    public boolean isEnableGithubAuth() {
        return enableGithubAuth;
    }

    public void setEnableGithubAuth(boolean enableGithubAuth) {
        this.enableGithubAuth = enableGithubAuth;
    }

    public Duration getTokenLifeTime() {
        return tokenLifeTime;
    }

    public void setTokenLifeTime(Duration tokenLifeTime) {
        this.tokenLifeTime = tokenLifeTime;
    }

    public String getBaseEmail() {
        return baseEmail;
    }

    public void setBaseEmail(String baseEmail) {
        this.baseEmail = baseEmail;
    }

    public String getAttachedRole() {
        return attachedRole;
    }

    public void setAttachedRole(String attachedRole) {
        this.attachedRole = attachedRole;
    }

    @Override
    public String getMarkerType() {
        return "token";
    }
}
