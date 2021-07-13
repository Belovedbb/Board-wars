package com.board.wars.domain.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class CustomConverter {
    public static SecurityContextImpl impl_converter_(SecurityContext context){
        Authentication authentication = context.getAuthentication();
        if(authentication instanceof OAuth2AuthenticationToken){
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            Object principalUser = context.getAuthentication().getPrincipal();
            CustomAuthUser oauthUser = getPrincipal(principalUser).orElseGet(CustomConverter::defaultUser);
            authentication = new OAuth2AuthenticationToken(oauthUser, resolveAuthorities(oAuth2AuthenticationToken.getAuthorities()), oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());
        }
        return new SecurityContextImpl(authentication);
    }

    private static Optional<CustomAuthUser> getPrincipal(Object principal){
        String sub = IdTokenClaimNames.SUB;
        if(principal instanceof DefaultOidcUser){
            DefaultOidcUser principalUser = (DefaultOidcUser) principal;
            Map<String, Object> attributes = mapClaimAttributes(principalUser.getIdToken(), principalUser.getUserInfo());
            return Optional.of(new CustomAuthUser(resolveAuthorities(principalUser.getAuthorities()),attributes, principalUser.getIdToken(), sub, principalUser.getUserInfo()));
        }
        else if(principal instanceof DefaultOAuth2User){
            DefaultOAuth2User principalUser = (DefaultOAuth2User) principal;
            sub = introspectNameAttribute(principalUser, sub);
            if(!CollectionUtils.isEmpty(principalUser.getAttributes())){
                return Optional.of(new CustomAuthUser(resolveAuthorities(principalUser.getAuthorities()),principalUser.getAttributes(), null, sub, null));
            }
            return Optional.of(new CustomAuthUser(resolveAuthorities(principalUser.getAuthorities()), Map.of(),null, sub, null));
        }
        return Optional.empty();
    }

    private static CustomAuthUser defaultUser(){
        return new CustomAuthUser(List.of(new SimpleGrantedAuthority("invalid")), Map.of(),null, IdTokenClaimNames.SUB, null);
    }

    private static Collection<? extends GrantedAuthority> resolveAuthorities(Collection<? extends GrantedAuthority> authorities){
        if(CollectionUtils.isEmpty(authorities)){
            return List.of();
        }
        return authorities.stream().map(CustomConverter::getAuthority).collect(Collectors.toList());
    }

    private static GrantedAuthority getAuthority(GrantedAuthority grantedAuthority){
        if(grantedAuthority instanceof OidcUserAuthority){
            OidcUserAuthority oidcAuthority = (OidcUserAuthority) grantedAuthority;
            return new CustomAuthority(oidcAuthority.getAuthority(), oidcAuthority.getAttributes(), oidcAuthority.getIdToken(), oidcAuthority.getUserInfo());
        }else if(grantedAuthority instanceof OAuth2UserAuthority){
            OAuth2UserAuthority oAuth2Authority = (OAuth2UserAuthority) grantedAuthority;
            return new CustomAuthority(oAuth2Authority.getAuthority(), oAuth2Authority.getAttributes(), null, null);
        }
        return grantedAuthority;
    }

    private static String introspectNameAttribute(DefaultOAuth2User principalUser, String defaultValue){
        String value = null;
        Field field = ReflectionUtils.findField(DefaultOAuth2User.class, "nameAttributeKey");
        if(field != null){
            ReflectionUtils.makeAccessible(field);
            value = (String) ReflectionUtils.getField(field, principalUser);
        }
        return value == null ? defaultValue : value;
    }

    private static Map<String, Object> mapClaimAttributes(OidcIdToken idToken, OidcUserInfo userInfo) {
        Map<String, Object> claims = new HashMap<>();
        if(idToken != null)  {
            if (userInfo != null) {
                claims.putAll(userInfo.getClaims());
            }
            claims.putAll(idToken.getClaims());
        }
        return claims;
    }
}
