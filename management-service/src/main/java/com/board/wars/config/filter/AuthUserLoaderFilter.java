package com.board.wars.config.filter;

import com.board.wars.config.properties.ApplicationProperties;
import com.board.wars.config.properties.RouteProperties;
import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.mapper.UserMapper;
import com.board.wars.marker.global.GlobalMarker;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.AuthUserResponse;
import com.board.wars.repository.UserRepository;
import com.board.wars.service.UserService;
import com.board.wars.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class AuthUserLoaderFilter implements WebFilter {
    final private WebClient webClient;
    final private RouteProperties routeProperties;

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    private ApplicationProperties appProperties;
    @Autowired
    UserService userService;

    public AuthUserLoaderFilter(WebClient webClient, RouteProperties routeProperties, UserRepository userRepository, UserMapper userMapper) {
        this.webClient = webClient;
        this.routeProperties = routeProperties;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String payloadToken = exchange.getRequest().getHeaders().toSingleValueMap().get(appProperties.getMarkerTokenHashKey());
        if(payloadToken == null) return chain.filter(exchange);
        final ServerHttpRequest request = exchange.getRequest();
        String unlinkedUserRoute = Utilities.resolveRoute(routeProperties.getIntermediate().getAuthServerManagementLink(), "false");
        return webClient.get().uri(unlinkedUserRoute).
                headers(httpHeaders -> httpHeaders.add(appProperties.getMarkerTokenHashKey(), request.getHeaders().toSingleValueMap().get(appProperties.getMarkerTokenHashKey())))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(AuthUserResponse.class)).
                flatMap(userResponse -> this.persistManagementUser(userResponse, exchange.getAttribute("marker"))).
                flatMap(user -> cleanUpAuthUser(user, request)).
                defaultIfEmpty(new User()).last().
                flatMap(user -> chain.filter(exchange));
    }

    private Mono<User> persistManagementUser(AuthUserResponse userResponse, GlobalMarker marker) {
        User authUser = userMapper.convertAuthUserToManagementUser(userResponse);
        return Mono.just(authUser)
                .map(user -> loadUpRole(user, marker))
                .flatMap(userRepository::save)
                .onErrorResume(error -> Mono.just(authUser));
    }

    private User loadUpRole(User user, GlobalMarker marker){
        UserRequestPayload payload = new UserRequestPayload();
        payload.setRole(marker.getEmail().equals(user.getUsername()) ? Role.ROLE_ADMIN : Role.ROLE_VISITOR);
        return userService.updateUserRole(user, marker, payload, "SYSTEM");
    }

    private Mono<User> cleanUpAuthUser(User user, ServerHttpRequest request) {
        AuthUserResponse authUserResponse = new AuthUserResponse();
        authUserResponse.setManagementLinked(true);
        return userService.updateAuthServerUser(user, request, authUserResponse)
                .onErrorResume(error -> Mono.empty())
                .map(entity -> user);
    }

}
