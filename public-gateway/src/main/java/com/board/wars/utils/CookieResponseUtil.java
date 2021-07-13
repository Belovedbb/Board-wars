package com.board.wars.utils;

import com.nimbusds.oauth2.sdk.util.MapUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CookieResponseUtil {

    public static String COOKIE_AUTH_BEARER_ID = "J_AUTH_USER_BEARER_ID";
    public static final String COOKIE_MARKER_TOKEN_HASH_KEY = "X_MARKER_PART_REG_HASH";
    private static CookieResponseUtil cookieResponseUtil = new CookieResponseUtil();

    private Optional<ResponseCookie> ensureSingleCookie(List<ResponseCookie> cookies){
        if(!CollectionUtils.isEmpty(cookies)){
            if(cookies.size() != 1){
                Optional<ResponseCookie> firstCandidate = cookies.stream().filter(Objects::nonNull).findFirst();
                cookies.clear();
                return firstCandidate;
            }else{
                return Optional.of(cookies.get(0));
            }
        }
        return Optional.empty();
    }

    public Optional<ResponseCookie> getCookieFromResponse(ServerHttpResponse response){
        MultiValueMap cookies = response.getCookies();
        if(cookies.isEmpty()){
            return getCookieForAttribute(response.getHeaders().toSingleValueMap(), COOKIE_AUTH_BEARER_ID, "Set-Cookie");
        }
        return getCookie(cookies, COOKIE_AUTH_BEARER_ID);
    }

    public Optional<ResponseCookie> getCookieFromRequest(ServerHttpRequest request){
        MultiValueMap cookies = request.getCookies();
        return getCookie(cookies, COOKIE_AUTH_BEARER_ID);
    }

    private static String resolveCookiePair(String rawCookie, String key){
        String stopper = ";";
        String rawValue = rawCookie.contains(stopper) ? rawCookie.substring(0, rawCookie.indexOf(stopper)) : rawCookie;
        if((!rawCookie.isEmpty() && !rawValue.contains(key) && rawCookie.contains(stopper))){
            //+ 1 account for stopper
            String newRawCookie = rawCookie.substring(rawValue.length() + 1).trim();
            rawValue = resolveCookiePair(newRawCookie, key);
        }
        return rawValue;
    }

    public Optional<ResponseCookie> getCookieForAttribute(Map<String, String> cookieMap, String key, String attribute){
        String keySetCookie = cookieMap.get(attribute);
        if(StringUtils.hasText(keySetCookie)){
            String rawValue =  resolveCookiePair(keySetCookie, key);
            if(StringUtils.hasText(rawValue) && rawValue.contains("=")) {
                String[] keyValuePair = rawValue.split("=", 2);
                if (key.equals(keyValuePair[0])) {
                    return Optional.of(buildCookie(keyValuePair[0], keyValuePair[1], -1));
                }
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Optional<ResponseCookie> getCookie(MultiValueMap cookies, String name) {
        if (MapUtils.isNotEmpty(cookies)) {
            List<ResponseCookie> allCookies = convertListCookieToResponseCookie((List<HttpCookie>) cookies.get(name));
            return ensureSingleCookie(allCookies);
        }
        return Optional.empty();
    }

    public ResponseCookie buildAuthCookie(String id, long age){
        return buildCookie(COOKIE_AUTH_BEARER_ID, id, age);
    }

    public ResponseCookie buildCookie(String key, String value, long age){
        return ResponseCookie.from(key, value)
                .maxAge(age)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .build();
    }

    public Supplier<ResponseCookie> getDefaultCookie(){
        return () -> buildCookie("_", "_", -1);
    }

    public static CookieResponseUtil getInstance(){
        return cookieResponseUtil;
    }

    private List<ResponseCookie> convertListCookieToResponseCookie(List<HttpCookie> httpCookie){
        List<ResponseCookie> responseCookies = new ArrayList<>();
        if(!CollectionUtils.isEmpty(httpCookie)){
            httpCookie.stream()
                    .filter(Objects::nonNull).map(this::convertCookieToResponseCookie)
                    .collect(Collectors.toCollection(() -> responseCookies));
        }
        return responseCookies;
    }

    private ResponseCookie convertCookieToResponseCookie(HttpCookie httpCookie){
        return ResponseCookie.fromClientResponse(httpCookie.getName(), httpCookie.getValue()).build();
    }

    private Map<String, ? extends HttpCookie> removeCookie(Map<String, ? extends HttpCookie> cookieMap, String... keys){
        Arrays.stream(keys).forEach(cookieMap::remove);
        return cookieMap;
    }

    private Map<String, ? extends HttpCookie>  clearRegisteredCookies(Map<String, ? extends HttpCookie> cookieMap){
        return removeCookie(cookieMap, COOKIE_AUTH_BEARER_ID, COOKIE_MARKER_TOKEN_HASH_KEY);
    }

    public ServerWebExchange removeAllCookies(ServerWebExchange exchange){
        exchange.getResponse().getHeaders().toSingleValueMap().remove("Set-Cookie");
        Map<String, ? extends HttpCookie> responseCookieMap = clearRegisteredCookies(exchange.getResponse().getCookies().toSingleValueMap());
        Map<String, ? extends HttpCookie> requestCookieMap =  clearRegisteredCookies(exchange.getRequest().getCookies().toSingleValueMap());
        exchange.getResponse().getCookies().setAll((Map<String, ResponseCookie>) responseCookieMap);
        ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
            httpHeaders.remove("Cookie");
            httpHeaders.add("Clear-Site-Data","cookies, cache");
        }).build();
        return  exchange.mutate().request(request).response(exchange.getResponse()).principal(exchange.getPrincipal()).build();
    }
}
