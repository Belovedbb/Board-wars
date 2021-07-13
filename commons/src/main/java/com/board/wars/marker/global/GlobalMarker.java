package com.board.wars.marker.global;

import com.board.wars.marker.OrganizationDetail;

public class GlobalMarker {

    private String id;

    private String applicationName;

    private String organizationName;

    private String fullName;

    private String email;

    private OrganizationDetail organization;

    private StorageMarker storageMarker;

    private TokenMarker tokenMarker;

    private RoleMarker roleMarker;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public StorageMarker getStorageMarker() {
        return storageMarker;
    }

    public void setStorageMarker(StorageMarker storageMarker) {
        this.storageMarker = storageMarker;
    }

    public TokenMarker getTokenMarker() {
        return tokenMarker;
    }

    public void setTokenMarker(TokenMarker tokenMarker) {
        this.tokenMarker = tokenMarker;
    }

    public RoleMarker getRoleMarker() {
        return roleMarker;
    }

    public void setRoleMarker(RoleMarker roleMarker) {
        this.roleMarker = roleMarker;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OrganizationDetail getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDetail organization) {
        this.organization = organization;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}

