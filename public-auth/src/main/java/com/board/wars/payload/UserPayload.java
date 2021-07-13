package com.board.wars.payload;


import com.board.wars.validator.annotations.ConfirmEmailMatcher;
import com.board.wars.validator.annotations.PasswordMatcher;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@PasswordMatcher
public class UserPayload implements Serializable {

    @NotNull
    @NotBlank(message = "Please provide a Valid Full Name")
    @Size(min = 4)
    private String fullName;

    @NotNull
    @Size(min = 6)
    @ConfirmEmailMatcher(tokenKey = "9Z7GJ6XKLQ5GV747APLI")
    private String email;

    @Size(min = 4)
    private String password;

    @NotNull
    @Size(min = 4)
    private String confirmPassword;

    private boolean terms;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private boolean enabled;

    private String pictureUrl;

    private String foreignToken;

    private boolean fullyLocal = true;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean getTerms() {
        return terms;
    }

    public void setTerms(Boolean terms) {
        this.terms = terms;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isTerms() {
        return terms;
    }

    public void setTerms(boolean terms) {
        this.terms = terms;
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

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
