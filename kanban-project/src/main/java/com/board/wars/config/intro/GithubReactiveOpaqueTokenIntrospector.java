package com.board.wars.config.intro;

import com.board.wars.GithubIntrospectionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

public class GithubReactiveOpaqueTokenIntrospector implements ReactiveOpaqueTokenIntrospector {

    private static final String GITHUB_INTROSPECTOR_URL= "https://api.github.com/user";
    private static final String GITHUB_SCOPE_HEADER = "x-oauth-scopes";
    private static final String GITHUB_CLIENT_HEADER = "x-oauth-client-id";
    private static final String authorityPrefix = "ROLE_";

    @Override
    public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
        return Mono.just(token)
                .flatMap(this::buildRequest)
                .flatMap(this::verifyResponseStatus)
                .flatMap(this::parseGithubResponse)
                .flatMap(this::isSuccessfullyParsed)
                .map(this::convertClaimsSet)
                .onErrorMap((e) -> !(e instanceof OAuth2IntrospectionException), this::onError);
    }

    private Mono<ClientResponse> buildRequest(String token) {
        WebClient webClient = WebClient.builder().build();
        return webClient.get()
                .uri(GITHUB_INTROSPECTOR_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange();
    }

    private Mono<ClientResponse> verifyResponseStatus(ClientResponse response){
        if(response.rawStatusCode() != HttpStatus.OK.value()){
            return Mono.error(new OAuth2IntrospectionException("Introspection endpoint responded with " + response.rawStatusCode()));
        }
        if(CollectionUtils.isEmpty(response.headers().header(GITHUB_SCOPE_HEADER))){
            return Mono.error(new OAuth2IntrospectionException("Introspection endpoint failed to acquire required scopes "));
        }
        return Mono.just(response);
    }

    private Mono<GithubIntrospectionResponse> parseGithubResponse(ClientResponse response){
        GithubIntrospectionResponse githubIntrospectionResponse = new GithubIntrospectionResponse();
        githubIntrospectionResponse.setRawHeaders(response.headers().header(GITHUB_SCOPE_HEADER), GithubIntrospectionResponse.scope_key)
        .setRawHeaders(response.headers().header(GITHUB_CLIENT_HEADER), GithubIntrospectionResponse.client_id_key);
        return response.bodyToMono(String.class)
                .doOnError(System.out::println)
                .doOnNext(e -> {System.out.println(e);githubIntrospectionResponse.setRawData(e);})
                .map(content -> githubIntrospectionResponse.parse());
    }

    private  Mono<GithubIntrospectionResponse> isSuccessfullyParsed(GithubIntrospectionResponse response){
        return  response.isActive() ? Mono.just(response) : Mono.just(response).then(Mono.error(new BadOpaqueTokenException("Provided token isn't active")));
    }

    private OAuth2AuthenticatedPrincipal convertClaimsSet(GithubIntrospectionResponse response) {
        Map<String, Object> claims = new HashMap<>();
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (response.getAudience() != null) {
            claims.put(OAuth2IntrospectionClaimNames.AUDIENCE, Collections.unmodifiableList(response.getAudience()));
        }

        if (response.getClientID() != null) {
            claims.put(OAuth2IntrospectionClaimNames.CLIENT_ID, response.getClientID());
        }
        if (response.getExpirationTime() != null) {
            claims.put(OAuth2IntrospectionClaimNames.EXPIRES_AT, response.getExpirationTime());
        }
        if (response.getIssueTime() != null) {
            claims.put(OAuth2IntrospectionClaimNames.ISSUED_AT, response.getIssueTime());
        }
        if (response.getIssuer() != null) {
            claims.put(OAuth2IntrospectionClaimNames.ISSUER, response.getIssuer());
        }
        if (response.getNotBeforeTime() != null) {
            claims.put(OAuth2IntrospectionClaimNames.NOT_BEFORE, response.getNotBeforeTime());
        }
        if (response.getScopes() != null) {
            List<String> scopes = response.getScopes();
            claims.put(OAuth2IntrospectionClaimNames.SCOPE, scopes);
            for (String scope : scopes) {
                authorities.add(new SimpleGrantedAuthority(authorityPrefix + scope));
            }
        }
        return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
    }


    private OAuth2IntrospectionException onError(Throwable ex) {
        return new OAuth2IntrospectionException(ex.getMessage(), ex);
    }

}
