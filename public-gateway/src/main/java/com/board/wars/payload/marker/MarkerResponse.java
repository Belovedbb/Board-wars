package com.board.wars.payload.marker;

public class MarkerResponse {

    private String organizationName;

    private String fullName;

    private String email;

    private StorageMarkerPayload storageMarkerPayload;

    private TokenMarkerPayload tokenMarkerPayload;

    private RoleMarkerPayload roleMarkerPayload;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
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

    public StorageMarkerPayload getStorageMarkerPayload() {
        return storageMarkerPayload;
    }

    public void setStorageMarkerPayload(StorageMarkerPayload storageMarkerPayload) {
        this.storageMarkerPayload = storageMarkerPayload;
    }

    public TokenMarkerPayload getTokenMarkerPayload() {
        return tokenMarkerPayload;
    }

    public void setTokenMarkerPayload(TokenMarkerPayload tokenMarkerPayload) {
        this.tokenMarkerPayload = tokenMarkerPayload;
    }

    public RoleMarkerPayload getRoleMarkerPayload() {
        return roleMarkerPayload;
    }

    public void setRoleMarkerPayload(RoleMarkerPayload roleMarkerPayload) {
        this.roleMarkerPayload = roleMarkerPayload;
    }
}
