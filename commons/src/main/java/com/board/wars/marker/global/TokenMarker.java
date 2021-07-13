package com.board.wars.marker.global;

public class TokenMarker {

    private String id;

    private boolean enableToken;

    private boolean enableGithubAuth;

    private Long tokenLifeTime;

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

    public Long getTokenLifeTime() {
        return tokenLifeTime;
    }

    public void setTokenLifeTime(Long tokenLifeTime) {
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
}
