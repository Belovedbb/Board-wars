package com.board.wars.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "route")
public class RouteProperties {
    private Internal internal;

    private Intermediate intermediate;

    private External external;

    public Internal getInternal() {
        return internal;
    }

    public void setInternal(Internal internal) {
        this.internal = internal;
    }

    public Intermediate getIntermediate() {
        return intermediate;
    }

    public void setIntermediate(Intermediate intermediate) {
        this.intermediate = intermediate;
    }

    public External getExternal() {
        return external;
    }

    public void setExternal(External external) {
        this.external = external;
    }

    public static class Internal {

    }

    public static class Intermediate {
        private String authServerManagementLink;

        private String authServerUpdateUser;

        public String getAuthServerManagementLink() {
            return authServerManagementLink;
        }

        public void setAuthServerManagementLink(String authServerManagementLink) {
            this.authServerManagementLink = authServerManagementLink;
        }

        public String getAuthServerUpdateUser() {
            return authServerUpdateUser;
        }

        public void setAuthServerUpdateUser(String authServerUpdateUser) {
            this.authServerUpdateUser = authServerUpdateUser;
        }
    }

    public static class External {

    }
}
