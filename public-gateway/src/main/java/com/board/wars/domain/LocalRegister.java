package com.board.wars.domain;

import com.board.wars.domain.marker.OrganizationDetail;

import java.util.List;
import java.util.Map;

public class LocalRegister {

    private Map<String, Object> attributes;

    private String organizationName;

    private String pictureUrl;

    private String fullName;

    private String email;

    private String password;

    private String confirmPassword;

    private boolean terms;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private boolean enabled;

    private String foreignToken;

    private boolean fullyLocal;

    private OrganizationDetail organization;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isTerms() {
        return terms;
    }

    public void setTerms(boolean terms) {
        this.terms = terms;
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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public OrganizationDetail getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDetail organization) {
        this.organization = organization;
    }
}
