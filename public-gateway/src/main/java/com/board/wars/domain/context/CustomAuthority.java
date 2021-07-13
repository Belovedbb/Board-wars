package com.board.wars.domain.context;


import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Document
public class CustomAuthority extends OAuth2UserAuthority{

    private final OidcIdToken idToken;

    private final OidcUserInfo userInfo;


    @PersistenceConstructor
    public CustomAuthority(String authority,Map<String, Object> attributes,  OidcIdToken idToken, OidcUserInfo userInfo) {
        super(authority, attributes);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }


    private static Map<String, Object> collectClaims(OidcIdToken idToken, OidcUserInfo userInfo) {
        Assert.notNull(idToken, "idToken cannot be null");
        Map<String, Object> claims = new HashMap<>();
        if (userInfo != null) {
            claims.putAll(userInfo.getClaims());
        }
        claims.putAll(idToken.getClaims());
        return claims;
    }
}

