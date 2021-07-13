package com.board.wars.service.marker;

import com.board.wars.domain.marker.parts.RoleMarkerPart;
import com.board.wars.mapper.MarkerMapper;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.payload.marker.RoleMarkerPayload;
import com.board.wars.store.marker.parts.RoleMarkerPartRepository;
import com.board.wars.utils.CookieResponseUtil;
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

import java.util.HashMap;
import java.util.Map;

@Service
public class RoleMarkerPartService implements MarkerPartService{
    private Map<String, String> validatedErrorMap = new HashMap<>();

    private final MarkerMapper markerMapper;
    private final RoleMarkerPartRepository roleMarkerPartRepository;

    public RoleMarkerPartService(MarkerMapper markerMapper, RoleMarkerPartRepository roleMarkerPartRepository) {
        this.markerMapper = markerMapper;
        this.roleMarkerPartRepository = roleMarkerPartRepository;
    }

    public <T> Flux<AuthenticatedResponse> serve(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request, T postBody){
        String currentToken = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        Assert.notNull(client, "client cant be null");
        Assert.notNull(currentToken, "token cant be null");
        Assert.isInstanceOf(RoleMarkerPayload.class, postBody);

        RoleMarkerPayload payload = (RoleMarkerPayload) postBody;
        validatedErrorMap.clear();
        RoleMarkerPart transformedRoleMarker = markerMapper.transformRoleFromPayloadToDomain(payload);
        return Mono.just(transformedRoleMarker)
                .map(this::validateStorageMarker)
                .filter(validatedStorage -> !StringUtils.hasText(validatedStorage.getT2()))
                .map(currentMarker -> Tuples.of(currentMarker.getT1(), roleMarkerPartRepository.findByPartId(currentToken)))
                .flatMap(this::updateRoleMarkerPart)
                .flatMap(roleMarkerPartRepository::save)
                .map(markerMapper -> AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_TOKEN, true))
                .switchIfEmpty(Mono.just(AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_ROLE, false)))
                .map(this::finalResponse)
                .flux();
    }

    private Tuple2<RoleMarkerPart, String> validateStorageMarker(RoleMarkerPart rolePart){
        StringBuilder validatorText = new StringBuilder();
        //TODO validation
        validatedErrorMap.put("storageMarkerError", validatorText.toString());
        return Tuples.of(rolePart, validatorText.toString());
    }

    private Mono<RoleMarkerPart> updateRoleMarkerPart(Tuple2<RoleMarkerPart, Mono<RoleMarkerPart>> detachedMarkerParts){
        RoleMarkerPart currentMarker = detachedMarkerParts.getT1();
        Mono<RoleMarkerPart> savedMarkerMono = detachedMarkerParts.getT2();
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
}
