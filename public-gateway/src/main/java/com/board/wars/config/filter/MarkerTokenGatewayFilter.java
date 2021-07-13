package com.board.wars.config.filter;

import com.board.wars.config.auth.properties.ApplicationProperties;
import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.mapper.MarkerMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MarkerTokenGatewayFilter implements GatewayFilter {

    private final ApplicationProperties appProperties;
    private final MarkerMapper mapper;

    public MarkerTokenGatewayFilter(ApplicationProperties appProperties, MarkerMapper mapper) {
        this.appProperties = appProperties;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .map(AbstractAuthenticationToken::getDetails)
                .filter(details -> details instanceof GlobalMarker)
                .cast(GlobalMarker.class)
                .map(mapper::convertToCommonsMarker)
                .flatMap(this::createMarkerClaimSet)
                .map(this::createJWEToken)
                .map(token -> injectMarkerJWE(exchange, token))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    private Mono<JWTClaimsSet> createMarkerClaimSet(com.board.wars.marker.global.GlobalMarker marker){
        return Mono.just(new JWTClaimsSet.Builder())
                .map(builder -> builder.claim(appProperties.getMarkerTokenHashClaim(), marker))
                .map(JWTClaimsSet.Builder::build);
    }

    private String createJWEToken(JWTClaimsSet claims)  {
        try {
            Payload payload = new Payload(claims.toJSONObject());
            JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512);
            byte[] secretKey = appProperties.getMarkerTokenHash().getBytes();
            DirectEncrypter encrypter = new DirectEncrypter(secretKey);
            JWEObject jweObject = new JWEObject(header, payload);
            jweObject.encrypt(encrypter);
            return jweObject.serialize();
        }catch (JOSEException ex){
            //TODO LOG AND THROW
            return "";
        }
    }

    private ServerWebExchange injectMarkerJWE(ServerWebExchange exchange, String jweToken) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(httpHeaders -> httpHeaders.add(appProperties.getMarkerTokenHashKey(), jweToken))
                .build();
        return exchange.mutate().request(request).build();
    }

}
