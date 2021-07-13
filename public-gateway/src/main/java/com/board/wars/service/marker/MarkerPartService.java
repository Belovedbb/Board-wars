package com.board.wars.service.marker;

import com.board.wars.domain.marker.parts.MarkerPart;
import com.board.wars.payload.AuthenticatedResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import reactor.core.publisher.Flux;

public interface MarkerPartService {

    default  <T extends MarkerPart> void updateMarkerPart(T savedPart, T currentPart){
        currentPart.setPartId(savedPart.getPartId());
        currentPart.setComplete(true);
    }

    <T> Flux<AuthenticatedResponse> serve(OAuth2AuthorizedClient client, OAuth2AuthenticationToken token, ServerHttpRequest request, T postBody);

}
