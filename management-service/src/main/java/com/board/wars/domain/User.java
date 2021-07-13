package com.board.wars.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Id is the primary identity i.e email
 */
@Document
public class User extends BaseEntity{
    @Indexed(unique = true)
    private String username;
    private String position;
    private Role role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean enabled;
    private boolean locked;
    private boolean expired;
    private String foreignToken;
    private boolean fullyLocal;
    private LocalDateTime authTimeCreated;
    private LocalDateTime timeRetrieved;
    private String pictureUrl;
    private String stateStatus;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public LocalDateTime getAuthTimeCreated() {
        return authTimeCreated;
    }

    public void setAuthTimeCreated(LocalDateTime authTimeCreated) {
        this.authTimeCreated = authTimeCreated;
    }

    public LocalDateTime getTimeRetrieved() {
        return timeRetrieved;
    }

    public void setTimeRetrieved(LocalDateTime timeRetrieved) {
        this.timeRetrieved = timeRetrieved;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getStateStatus() {
        return stateStatus;
    }

    public void setStateStatus(String stateStatus) {
        this.stateStatus = stateStatus;
    }
}
