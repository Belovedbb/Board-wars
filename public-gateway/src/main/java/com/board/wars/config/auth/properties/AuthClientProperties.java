package com.board.wars.config.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Component
@Validated
@ConfigurationProperties(prefix = "auth.client")
public class AuthClientProperties {
    @NotNull
    private CookieProperties cookie;

    public CookieProperties getCookie() {
        return cookie;
    }

    public void setCookie(CookieProperties cookie) {
        this.cookie = cookie;
    }

    static public class CookieProperties{
        private long age;
        private boolean secure;

        public long getAge() {
            return age;
        }

        public void setAge(long age) {
            this.age = age;
        }

        public boolean isSecure() {
            return secure;
        }

        public void setSecure(boolean secure) {
            this.secure = secure;
        }
    }
}
