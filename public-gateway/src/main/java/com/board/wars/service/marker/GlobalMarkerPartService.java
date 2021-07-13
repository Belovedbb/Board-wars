package com.board.wars.service.marker;

import com.board.wars.domain.LocalRegister;
import com.board.wars.domain.marker.OrganizationDetail;
import com.board.wars.domain.marker.parts.GlobalMarkerPart;
import com.board.wars.mapper.MarkerMapper;
import com.board.wars.payload.AuthenticatedResponse;
import com.board.wars.store.marker.parts.GlobalMarkerPartRepository;
import com.board.wars.utils.CookieResponseUtil;
import com.board.wars.utils.RouteUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;

@Service
public class GlobalMarkerPartService implements MarkerPartService{

    final private MarkerMapper markerMapper;
    private final GlobalMarkerPartRepository globalMarkerPartRepository;
    private final WebClient webClient;
    private Map<String, String> validatedErrorMap = new HashMap<>();

    public GlobalMarkerPartService(MarkerMapper markerMapper, GlobalMarkerPartRepository globalMarkerPartRepository, WebClient webClient) {
        this.markerMapper = markerMapper;
        this.globalMarkerPartRepository = globalMarkerPartRepository;
        this.webClient = webClient;
    }

    public <T> Flux<AuthenticatedResponse> serve(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token,  ServerHttpRequest request, T postBody){
        String currentToken = request.getCookies().toSingleValueMap().get(CookieResponseUtil.COOKIE_MARKER_TOKEN_HASH_KEY).getValue();
        Assert.notNull(client, "client cant be null");
        Assert.notNull(currentToken, "token cant be null");
        validatedErrorMap.clear();
        LocalRegister localRegister = markerMapper.transformAuthenticationTokenToLocalRegister(token, client);
        //TODO use validation API
        return Mono.just(localRegister)
                .map(this::validateLocalRegister)
                .filter(validatedRegister -> !StringUtils.hasText(validatedRegister.getT2()))
                .flatMap(this::registerGlobalMarkerWithAuthService)
                .map(markerMapper::transformLocalRegisterToGlobalPart)
                .map(currentMarker -> Tuples.of(currentMarker, globalMarkerPartRepository.findByPartId(currentToken)))
                .flatMap(this::updateGlobalMarkerPart)
                .flatMap(globalMarkerPartRepository::save)
                .map(markerMapper -> AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_STORAGE, true))
                .switchIfEmpty(Mono.just( AuthenticatedResponse.populateResponse(AuthenticatedResponse.Level.ORGANIZATION_GLOBAL, false)))
                .map(this::finalResponse)
                .flux();
    }

    private Tuple2<LocalRegister, String> validateLocalRegister(LocalRegister localRegister){
        StringBuilder validatorText = new StringBuilder();
        if(!StringUtils.hasText(localRegister.getOrganizationName())){
            validatorText.append("No organization is registered").append("\n");
        }else if(localRegister.getOrganization() != null){
            OrganizationDetail organizationDetail = localRegister.getOrganization();
            if(!StringUtils.hasText(organizationDetail.getUrl())){
                validatorText.append("organization url details cant be empty");
            }
        }
        validatedErrorMap.put("registrationError", validatorText.toString());
        return Tuples.of(localRegister, validatorText.toString());
    }

    private Mono<LocalRegister> registerGlobalMarkerWithAuthService(Tuple2<LocalRegister, String> validatedRegisterTuple){
        LocalRegister validatedRegister = validatedRegisterTuple.getT1();
        return webClient.post()
                .uri(RouteUtil.Intermediate.ENDPOINT_PUBLIC_AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(validatedRegister), LocalRegister.class)
                .retrieve()
                .onStatus(status -> status != HttpStatus.CREATED, response -> Mono.just(new IllegalStateException("unable to register user org: "+ response.statusCode().name())))
                .toBodilessEntity()
                //TODO LOG ERROR
                .onErrorResume(error -> {
                    validatedErrorMap.put("localAuthError", error.getMessage());
                    return Mono.empty();
                })
                .flatMap(responseStat -> Mono.just(validatedRegister));
    }

    private Mono<GlobalMarkerPart> updateGlobalMarkerPart(Tuple2<GlobalMarkerPart, Mono<GlobalMarkerPart>> detachedMarkerParts){
        GlobalMarkerPart currentMarker = detachedMarkerParts.getT1();
        Mono<GlobalMarkerPart> savedMarkerMono = detachedMarkerParts.getT2();
        return savedMarkerMono.doOnNext(savedMarker -> {
            currentMarker.setId(savedMarker.getId());
            updateMarkerPart(savedMarker, currentMarker);
        }).flatMap(savedMarker -> Mono.just(currentMarker));
    }


    private AuthenticatedResponse finalResponse(AuthenticatedResponse authenticatedResponse){
        //if auth response is still global,  still contain error then or status is false
        if(!authenticatedResponse.isStatus() ){
            authenticatedResponse.setValue(validatedErrorMap.toString());
        }
        return authenticatedResponse;
    }
}
