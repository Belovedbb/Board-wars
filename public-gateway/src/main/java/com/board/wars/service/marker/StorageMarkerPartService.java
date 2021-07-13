package com.board.wars.service.marker;

import com.board.wars.domain.marker.parts.StorageMarkerPart;
import com.board.wars.mapper.MarkerMapper;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.payload.marker.StorageMarkerPayload;
import com.board.wars.store.marker.parts.StorageMarkerPartRepository;
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
public class StorageMarkerPartService implements MarkerPartService {
    private Map<String, String> validatedErrorMap = new HashMap<>();

    private final MarkerMapper markerMapper;
    private final StorageMarkerPartRepository storageMarkerPartRepository;

    public StorageMarkerPartService(MarkerMapper markerMapper, StorageMarkerPartRepository storageMarkerPartRepository) {
        this.markerMapper = markerMapper;
        this.storageMarkerPartRepository = storageMarkerPartRepository;
    }

    @Override
    public <T> Flux<AuthenticatedResponse> serve(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token, ServerHttpRequest request, T postBody) {
        String currentToken = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        Assert.notNull(client, "client cant be null");
        Assert.notNull(currentToken, "token cant be null");
        Assert.isInstanceOf(StorageMarkerPayload.class, postBody);

        StorageMarkerPayload payload = (StorageMarkerPayload) postBody;
        validatedErrorMap.clear();
        StorageMarkerPart transformedPart = markerMapper.transformStorageFromPayloadToDomain(payload);
        return Mono.just(transformedPart)
                .map(this::validateStorageMarker)
                .filter(validatedStorage -> !StringUtils.hasText(validatedStorage.getT2()))
                .map(currentMarker -> Tuples.of(currentMarker.getT1(), storageMarkerPartRepository.findByPartId(currentToken)))
                .flatMap(this::updateStorageMarkerPart)
                .flatMap(storageMarkerPartRepository::save)
                .map(markerMapper -> AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_ROLE, true))
                .switchIfEmpty(Mono.just(AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_STORAGE, false)))
                .map(this::finalResponse)
                .flux();
    }

    private Tuple2<StorageMarkerPart, String> validateStorageMarker(StorageMarkerPart storagePart){
        StringBuilder validatorText = new StringBuilder();
        if(storagePart.getType() == null){
            validatorText.append("storage type cant be empty");
        }
        validatedErrorMap.put("storageMarkerError", validatorText.toString());
        return Tuples.of(storagePart, validatorText.toString());
    }

    private Mono<StorageMarkerPart> updateStorageMarkerPart(Tuple2<StorageMarkerPart, Mono<StorageMarkerPart>> detachedMarkerParts){
        StorageMarkerPart currentMarker = detachedMarkerParts.getT1();
        Mono<StorageMarkerPart> savedMarkerMono = detachedMarkerParts.getT2();
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
