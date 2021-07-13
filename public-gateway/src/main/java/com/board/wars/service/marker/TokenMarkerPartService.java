package com.board.wars.service.marker;

import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import com.board.wars.domain.marker.parts.TokenMarkerPart;
import com.board.wars.mapper.MarkerMapper;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.payload.marker.MarkerResponse;
import com.board.wars.payload.marker.TokenMarkerPayload;
import com.board.wars.store.marker.parts.GlobalMarkerPartRepository;
import com.board.wars.store.marker.parts.RoleMarkerPartRepository;
import com.board.wars.store.marker.parts.StorageMarkerPartRepository;
import com.board.wars.store.marker.parts.TokenMarkerPartRepository;
import com.board.wars.utils.CookieResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenMarkerPartService implements MarkerPartService{
    private Map<String, String> validatedErrorMap = new HashMap<>();

    private final MarkerMapper markerMapper;
    private final TokenMarkerPartRepository tokenMarkerPartRepository;
    private final GlobalMarkerService globalMarkerService;


    public TokenMarkerPartService(MarkerMapper markerMapper, TokenMarkerPartRepository tokenMarkerPartRepository, GlobalMarkerService globalMarkerService) {
        this.markerMapper = markerMapper;
        this.tokenMarkerPartRepository = tokenMarkerPartRepository;
        this.globalMarkerService = globalMarkerService;
    }

    @Override
    public <T> Flux<AuthenticatedResponse> serve(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token, ServerHttpRequest request, T postBody) {
        String currentToken = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        Assert.notNull(client, "client cant be null");
        Assert.notNull(currentToken, "token cant be null");
        Assert.isInstanceOf(TokenMarkerPayload.class, postBody);

        TokenMarkerPayload payload = (TokenMarkerPayload) postBody;
        validatedErrorMap.clear();
        TokenMarkerPart transformedPart = markerMapper.transformTokenFromPayloadToDomain(payload);
        return Mono.just(transformedPart)
                .map(this::validateTokenMarker)
                .filter(validatedToken -> !StringUtils.hasText(validatedToken.getT2()))
                .map(currentMarker -> Tuples.of(currentMarker.getT1(), tokenMarkerPartRepository.findByPartId(currentToken)))
                .flatMap(this::updateTokenMarkerPart)
                .flatMap(tokenMarkerPartRepository::save)
                .flatMap(markerMapper -> loadUpSuccessfulAuthenticatedResponse(markerMapper.getPartId()))
                .switchIfEmpty(Mono.just(AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_TOKEN, false)))
                .map(this::finalResponse)
                .flux();
    }

    private Tuple2<TokenMarkerPart, String> validateTokenMarker(TokenMarkerPart tokenPart){
        StringBuilder validatorText = new StringBuilder();
        //TODO validation
        return Tuples.of(tokenPart, validatorText.toString());
    }

    private Mono<TokenMarkerPart> updateTokenMarkerPart(Tuple2<TokenMarkerPart, Mono<TokenMarkerPart>> detachedMarkerParts){
        TokenMarkerPart currentMarker = detachedMarkerParts.getT1();
        Mono<TokenMarkerPart> savedMarkerMono = detachedMarkerParts.getT2();
        return savedMarkerMono.doOnNext(savedMarker -> {
            currentMarker.setId(savedMarker.getId());
            updateMarkerPart(savedMarker, currentMarker);
        }).flatMap(savedMarker -> Mono.just(currentMarker));
    }

    private AuthenticatedResponse finalResponse(AuthenticatedResponse authenticatedResponse){
        if(!StringUtils.hasText(authenticatedResponse.getValue())){
            authenticatedResponse.setValue(validatedErrorMap.toString());
        }
        return authenticatedResponse;
    }

    private Mono<AuthenticatedResponse> loadUpSuccessfulAuthenticatedResponse(String partId){
        return globalMarkerService.loadUpGlobalMarker(partId)
                .flatMap(this::handleGlobalMarker)
                .map(markerMapper::transformGlobalPartToMarkerResponse)
                .map(response -> new AuthenticatedResponse(true, AuthenticatedResponse.AUTHENTICATED_ORGANIZATION_MESSAGE, AuthenticatedResponse.Level.INTERNAL, LocalDateTime.now(), response));
    }

    private Mono<GlobalMarker> handleGlobalMarker(GlobalMarker marker){
        if(!StringUtils.hasText(marker.getOrganizationName())){
            validatedErrorMap.put("finalResult", "unable to load up response");
            return Mono.empty();
        }
        return Mono.just(marker);
    }

}
