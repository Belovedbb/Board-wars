package com.board.wars.config.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RewritePathWithClientRegistrationGatewayFilter implements GatewayFilter {

    @Autowired
    RewritePathGatewayFilterFactory rewritePathFilterInstance;

    private String regex;
    private String replacement;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .map(token -> rewritePathFilterInstance.apply(c -> c.setRegexp(regex).setReplacement("/" + token.getAuthorizedClientRegistrationId() + replacement)))
                .flatMap(e -> e.filter(exchange, chain));
    }


    public void setRegexAndReplacement(String regex, String replacement) {
        this.regex = regex;
        this.replacement = replacement;
    }

    public String getReplacement() {
        return replacement;
    }

    public String getRegex() {
        return regex;
    }
}
