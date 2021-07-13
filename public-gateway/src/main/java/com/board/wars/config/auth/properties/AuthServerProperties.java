package com.board.wars.config.auth.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@Validated
@ConfigurationProperties(prefix = "auth.server")
public class AuthServerProperties {

    @NotNull
    private GithubProperties githubProperties;

    @NotNull
    private LocalProperties localProperties;

    public GithubProperties getGithubProperties() {
        return githubProperties;
    }

    public void setGithubProperties(GithubProperties githubProperties) {
        this.githubProperties = githubProperties;
    }

    public LocalProperties getLocalProperties() {
        return localProperties;
    }

    public void setLocalProperties(LocalProperties localProperties) {
        this.localProperties = localProperties;
    }

    static public class GithubProperties{
        private String ref;
        private String id;
        private String secret;
        private String redirect;
        private String authorize;
        private String token;
        private String name;
        private String userInfo;
        private String userInfoName;
        private List<String> scopes;

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }

        public String getAuthorize() {
            return authorize;
        }

        public void setAuthorize(String authorize) {
            this.authorize = authorize;
        }

        public String getToken() {
            return token;
        }

        public String getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(String userInfo) {
            this.userInfo = userInfo;
        }

        public String getUserInfoName() {
            return userInfoName;
        }

        public void setUserInfoName(String userInfoName) {
            this.userInfoName = userInfoName;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }
    }

    static public class LocalProperties{
        private String issuer;
        private String ref;
        private String id;
        private String name;
        private String secret;
        private String redirect;
        private List<String> scopes;

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}
