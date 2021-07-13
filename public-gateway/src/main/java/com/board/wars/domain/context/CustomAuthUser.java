package com.board.wars.domain.context;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Document(collection="CustomAuthUser")
public class CustomAuthUser  extends CustomDefaultOAuth2User implements OidcUser {

    private  OidcIdToken idToken;

    private  OidcUserInfo userInfo;

    @PersistenceConstructor
    public CustomAuthUser(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, OidcIdToken idToken,
                          String nameAttributeKey, OidcUserInfo userInfo) {
        super(authorities, attributes, nameAttributeKey);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public void setIdToken(OidcIdToken idToken) {
        this.idToken = idToken;
    }

    public void setUserInfo(OidcUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.getAttributes();
    }

    @Override
    public OidcIdToken getIdToken() {
        return this.idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return this.userInfo;
    }
}
