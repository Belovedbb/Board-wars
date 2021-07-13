package com.board.wars.config;

import com.board.wars.marker.global.GlobalMarker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.text.ParseException;
import java.util.List;

@Component
public class MarkerHttpSessionSecurityContextRepository extends HttpSessionSecurityContextRepository {

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ApplicationProperties appProperties;
    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        String payloadToken = request.getHeader(appProperties.getMarkerTokenHashKey());
        if (StringUtils.hasText(payloadToken)){
            ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(appProperties.getMarkerTokenHash().getBytes());
            JWEKeySelector<SimpleSecurityContext> jweKeySelector =
                    new JWEDecryptionKeySelector<>(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, jweKeySource);
            jwtProcessor.setJWEKeySelector(jweKeySelector);
            JWTClaimsSet claims = null;
            try {
                claims = jwtProcessor.process(payloadToken, null);
            } catch (ParseException | BadJOSEException | JOSEException e) {
                e.printStackTrace();
            }
            JSONObject object = (JSONObject) claims.getClaim(appProperties.getMarkerTokenHashClaim());
            objectMapper.registerModule(new JavaTimeModule());
            GlobalMarker marker = null;
            try {
                marker = objectMapper.readValue(object.toJSONString(), GlobalMarker.class);
                if (StringUtils.hasText(marker.getEmail())) {
                    request.getSession();
                    Authentication authenticatedUser = new UsernamePasswordAuthenticationToken((Principal) marker::getOrganizationName, "", List.of(new SimpleGrantedAuthority("admin")));
                    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
                    request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return super.loadContext(requestResponseHolder);
    }



}
