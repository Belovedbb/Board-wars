package com.board.wars.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Document(value = "user")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private List<Role> roles;
    private String phoneNumber;
    private boolean enabled;
    private boolean locked;
    private boolean expired;
    private String foreignToken;
    private boolean fullyLocal;
    private String pictureUrl;
    private boolean managementLinked;
    @CreatedDate
    private LocalDateTime timeCreated;
    @LastModifiedBy
    private
    String lastModifiedUser;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public User(){}

    public User(String email, String password, List<Role> roles, String firstName, String lastName, String phoneNumber,
                boolean enabled, String foreignToken, boolean fullyLocal) {
        this(null, email, password, roles,firstName, lastName, phoneNumber, enabled, false, false,
                foreignToken, fullyLocal, null, false, null, null,
                null);
    }

    public User(String id, String email, String password,
                List<Role> roles,String firstName, String lastName, String phoneNumber,
                boolean enabled, boolean locked, boolean expired,
                String foreignToken, boolean fullyLocal, String pictureUrl, boolean managementLinked,
                LocalDateTime timeCreated, String lastModifiedUser, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.enabled = enabled;
        this.locked = locked;
        this.expired = expired;
        this.foreignToken = foreignToken;
        this.fullyLocal = fullyLocal;
        this.pictureUrl = pictureUrl;
        this.timeCreated = timeCreated;
        this.lastModifiedUser = lastModifiedUser;
        this.lastModifiedDate = lastModifiedDate;
        this.managementLinked = managementLinked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public void setForeignToken(String foreignToken) {
        this.foreignToken = foreignToken;
    }

    public boolean isFullyLocal() {
        return fullyLocal;
    }

    public void setFullyLocal(boolean fullyLocal) {
        this.fullyLocal = fullyLocal;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map( role -> new SimpleGrantedAuthority(role.getRole())).collect(Collectors.toList());
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public boolean isManagementLinked() {
        return managementLinked;
    }

    public void setManagementLinked(boolean managementLinked) {
        this.managementLinked = managementLinked;
    }
}
