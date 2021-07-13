package com.board.wars.controller;

import com.board.wars.domain.marker.global.GlobalMarker;
import com.board.wars.service.MarkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private MarkerService markerService;
//
//    @GetMapping
//    Flux<String> home(OAuth2AuthenticationToken token){
//        return Flux.just(String.valueOf(token.getCredentials()), token.getAuthorizedClientRegistrationId(), String.valueOf(token.getDetails()));
//    }

    @GetMapping("/global-marker")
    Mono<GlobalMarker> getGlobalMarkerObject(Mono<Principal> principal) {
        return this.markerService.getGlobalMarkerObject(principal);
    }

    @PatchMapping("/global-marker")
    Mono<GlobalMarker> updateGlobalMarkerObject(@RequestBody GlobalMarker globalMarker) {
        return this.markerService.updateGlobalMarker(globalMarker);
    }


}
