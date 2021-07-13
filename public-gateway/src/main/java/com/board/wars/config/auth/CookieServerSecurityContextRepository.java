package com.board.wars.config.auth;

import com.board.wars.config.auth.properties.AuthClientProperties;
import com.board.wars.domain.InlineContainer;
import com.board.wars.domain.context.CustomConverter;
import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.init.MarkerStarter;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.InlineContainerUtil;
import com.board.wars.utils.RouteUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Component
public class CookieServerSecurityContextRepository implements ServerSecurityContextRepository {

    private static final CookieResponseUtil cookieHelper = CookieResponseUtil.getInstance();
    private final InlineContainerUtil inlineContainerUtil;
    private final AuthClientProperties clientProperties;
    final private MarkerStarter markerStarter;
    final ServerRequestCache requestCache;

    CookieServerSecurityContextRepository(InlineContainerUtil inlineContainerUtil, AuthClientProperties clientProperties,
                                          MarkerStarter markerStarter, ServerRequestCache requestCache){
        this.inlineContainerUtil = inlineContainerUtil;
        this.clientProperties = clientProperties;
        this.markerStarter = markerStarter;
        this.requestCache = requestCache;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        ResponseCookie responseCookie  = cookieHelper.getCookieFromRequest(exchange.getRequest())
                .orElse(cookieHelper.getCookieFromResponse(exchange.getResponse()).orElse(null));
        final long authCookieAge = clientProperties.getCookie().getAge();
        if(context != null){
            SecurityContextImpl securityContext = CustomConverter.impl_converter_(context);
            if (responseCookie == null) {
                return markerStarter.getGlobalMarker()
                        .defaultIfEmpty(new GlobalMarker())
                        .map(marker -> insertTokenDetail(securityContext, marker))
                        .flatMap(securityCon -> buildContextRepository(securityContext, authCookieAge))
                        .flatMap(containerId -> attachContextAnchor(containerId, exchange, authCookieAge));
            }
        }else if(responseCookie != null && StringUtils.hasText(responseCookie.getValue())){
            return inlineContainerUtil.removeInlineContainer(responseCookie.getValue());
        }
        return Mono.empty();
    }

    private SecurityContextImpl insertTokenDetail(SecurityContextImpl securityContext, GlobalMarker marker){
        Authentication auth = securityContext.getAuthentication();
        if(auth instanceof OAuth2AuthenticationToken){
            ((OAuth2AuthenticationToken) auth).setDetails(marker);
        }
        return securityContext;
    }

    private Mono<String> buildContextRepository(SecurityContextImpl securityContext, long authCookieAge){
        return inlineContainerUtil.linkContextUser(securityContext)
                .linkContainer(Duration.ofSeconds(authCookieAge))
                .build();
    }

    private Mono<Void> attachContextAnchor(String containerId, ServerWebExchange exchange,  long authCookieAge){
        ResponseCookie cookie = cookieHelper.buildAuthCookie(containerId, authCookieAge);
        exchange.getResponse().getHeaders().add(HttpHeaders.SET_COOKIE, String.valueOf(cookie));
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        ResponseCookie responseCookie  = cookieHelper.getCookieFromRequest(exchange.getRequest()).orElse(null);
        if(responseCookie != null) {
            String value = StringUtils.hasText(responseCookie.getValue()) ? responseCookie.getValue() : "";
            return inlineContainerUtil.getInlineContainer(value).flatMap(this::getSecurityContext);
        }else{
            saveRequestCache(exchange);
            return Mono.empty();
        }
    }

    private void saveRequestCache(ServerWebExchange exchange){
        URI exchangeUri = exchange.getRequest().getURI();
        boolean hasQueryPath = StringUtils.hasText(exchangeUri.getPath()) && StringUtils.hasText(exchangeUri.getQuery());
        if(hasQueryPath &&
                (exchangeUri.getPath().equals(RouteUtil.Internal.AUTH_GITHUB_ENTRY) ||
                        exchangeUri.getPath().equals(RouteUtil.Internal.AUTH_LOCAL_ENTRY))
                && exchangeUri.getQuery().contains(RouteUtil.Internal.AUTH_POPUP_RAW_QUERY)){
             requestCache.saveRequest(exchange).thenReturn("saved").subscribe();
        }
    }

    private Mono<SecurityContext> getSecurityContext(InlineContainer container){
        if (container != null) {
            return Mono.justOrEmpty(container.getContextUser().getContext());
        }
        return Mono.empty();
    }

}
