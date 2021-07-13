package com.board.wars.config.auth;

import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.InlineContainerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class LogoutHandler implements ServerLogoutSuccessHandler {
    private final InlineContainerUtil containerUtil;

    public LogoutHandler(InlineContainerUtil containerUtil) {
        this.containerUtil = containerUtil;
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        ServerHttpResponse response = exchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND);
        //response.getHeaders().setLocation(URI.create("/auth"));
        response.getCookies().remove(CookieResponseUtil.COOKIE_AUTH_BEARER_ID);
        ResponseCookie cookie = CookieResponseUtil.getInstance().getCookieFromRequest(exchange.getExchange().getRequest()).orElse(null);
        return cookie == null ? Mono.empty() : containerUtil.removeInlineContainer(cookie.getValue());
    }


}
