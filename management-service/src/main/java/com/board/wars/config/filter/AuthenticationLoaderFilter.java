package com.board.wars.config.filter;

import com.board.wars.config.properties.ApplicationProperties;
import com.board.wars.marker.global.GlobalMarker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.InvalidJsonException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.shaded.json.JSONObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(0)
public class AuthenticationLoaderFilter  implements WebFilter {

    private final ObjectMapper objectMapper;

    private final ApplicationProperties appProperties;

    public AuthenticationLoaderFilter(ObjectMapper objectMapper, ApplicationProperties appProperties) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            String payloadToken = exchange.getRequest().getHeaders().toSingleValueMap().get(appProperties.getMarkerTokenHashKey());
            if(payloadToken == null) throw new InvalidJsonException("");
            ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(appProperties.getMarkerTokenHash().getBytes());
            JWEKeySelector<SimpleSecurityContext> jweKeySelector =
                    new JWEDecryptionKeySelector<>(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, jweKeySource);
            jwtProcessor.setJWEKeySelector(jweKeySelector);
            JWTClaimsSet claims = jwtProcessor.process(payloadToken, null);
            JSONObject object = (JSONObject) claims.getClaim(appProperties.getMarkerTokenHashClaim());
            objectMapper.registerModule(new JavaTimeModule());
            GlobalMarker marker = objectMapper.readValue(object.toJSONString(), GlobalMarker.class);
            exchange.getAttributes().put("marker", marker);
            return chain.filter(exchange);
        }catch (Exception ex){
            return chain.filter(exchange);
        }
    }
}
