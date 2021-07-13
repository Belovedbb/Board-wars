package com.board.wars.config.auth;

import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.domain.marker.parts.MarkerPart;
import com.board.wars.init.MarkerStarter;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.MarkerContainerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class AuthServerAuthenticationSuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    @Autowired
    ServerRequestCache requestCache;

    final private MarkerStarter markerStarter;
    final private MarkerContainerUtil markerUtil;
    private final CookieServerSecurityContextRepository cookieServerSecurityContextRepository;
    private static final CookieResponseUtil cookieHelper = CookieResponseUtil.getInstance();

    private URI locationSuccess = URI.create("/marker/global");
    private URI locationFailure = URI.create("/logout");

    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    public AuthServerAuthenticationSuccessHandler(MarkerStarter markerStarter, MarkerContainerUtil markerUtil, CookieServerSecurityContextRepository cookieServerSecurityContextRepository) {
        this.markerStarter = markerStarter;
        this.markerUtil = markerUtil;
        this.cookieServerSecurityContextRepository = cookieServerSecurityContextRepository;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return markerStarter.getGlobalMarker()
                .defaultIfEmpty(new GlobalMarker())
                .flatMap(marker -> switcher(marker, webFilterExchange, authentication));
    }

    private Mono<Void> switcher(GlobalMarker marker, WebFilterExchange webFilterExchange, Authentication authentication){
        return StringUtils.hasText(marker.getId()) ?
                super.onAuthenticationSuccess(webFilterExchange, authentication) :
                markerRedirectStrategy(webFilterExchange.getExchange(), authentication);
    }

    private Mono<Void> markerRedirectStrategy(ServerWebExchange exchange, Authentication authentication){
        return Mono.just(authentication)
                .filter(principal -> principal instanceof OAuth2AuthenticationToken)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(token -> getStrategyGateway(token, exchange, authentication));
    }

    private Mono<Void> getStrategyGateway(OAuth2AuthenticationToken token, ServerWebExchange exchange, Authentication authentication){
        if(!MarkerContainerUtil.PRINCIPAL_AUTH_MARKER_SERVER.contains(token.getAuthorizedClientRegistrationId().toLowerCase())) {
            return cookieServerSecurityContextRepository.save(exchange, null)
                    .then(redirectStrategy.sendRedirect(exchange.mutate().principal(Mono.empty()).build(), locationFailure));
        }else{
            return markerUtil.createNewPartInstances()
                    .injectHash()
                    .buildPart()
                    .flatMap(markerPart -> sendSuccessRedirect(markerPart, exchange));
        }
    }

    private Mono<Void> sendSuccessRedirect(MarkerPart markerPart, ServerWebExchange exchange){
        ResponseCookie cookie = cookieHelper.buildCookie(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY, markerPart.getPartId(), 2 * 60 * 60);
        exchange.getResponse().getCookies().set(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY, cookie);
        return redirectStrategy.sendRedirect(exchange, locationSuccess);
    }

}