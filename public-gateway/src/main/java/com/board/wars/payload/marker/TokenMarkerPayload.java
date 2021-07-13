package com.board.wars.payload.marker;

public class TokenMarkerPayload {
    private boolean allowTokenPass;
    private String baseEmail;
    private String attachedRole;
    //seconds
    private Long expiryPeriod;

    public boolean isAllowTokenPass() {
        return allowTokenPass;
    }

    public void setAllowTokenPass(boolean allowTokenPass) {
        this.allowTokenPass = allowTokenPass;
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

    public Long getExpiryPeriod() {
        return expiryPeriod;
    }

    public void setExpiryPeriod(Long expiryPeriod) {
        this.expiryPeriod = expiryPeriod;
    }
}
