package com.board.wars.payload;

import com.board.wars.util.Constant;

import java.time.LocalDateTime;

public class UserResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String roles;
    private String phoneNumber;
    private boolean enabled;
    private boolean locked;
    private boolean expired;
    private String foreignToken;
    private boolean fullyLocal;
    private LocalDateTime timeCreated;
    private LocalDateTime timeRetrieved;
    private String pictureUrl;
    private String stateStatus;
    private boolean isManagementLinked;

    public UserResponse(){
        setStateStatus(Constant.SUCCESSFUL_STATE_MESSAGE);
    }

    public UserResponse(String stateStatusMessage){
        setStateStatus(stateStatusMessage);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getForeignToken() {
        return foreignToken;
    }

    public void setForeignToken(String foreignToken) {
        this.foreignToken = foreignToken;
    }

    public boolean isFullyLocal() {
        return fullyLocal;
    }

    public void setFullyLocal(boolean fullyLocal) {
        this.fullyLocal = fullyLocal;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(String stateStatus) {
        this.stateStatus = stateStatus;
    }

    public LocalDateTime getTimeRetrieved(){
        return LocalDateTime.now();
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public boolean isManagementLinked() {
        return isManagementLinked;
    }

    public void setManagementLinked(boolean managementLinked) {
        isManagementLinked = managementLinked;
    }
}
