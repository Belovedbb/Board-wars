package com.board.wars.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

public class RouteUtil {

    static final public String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36";
    static public class  Internal{
        final public static  String ENDPOINT_LOGIN_ERROR = "/login?error";
        final public static String API_VERSION_ROUTE = "/api/v1/";
        final public static String AUTH_POPUP_RAW_QUERY = "popup=true";
        final public static String AUTH_GITHUB_ENTRY ="/oauth2/authorization/github";
        final public static String AUTH_LOCAL_ENTRY ="/oauth2/authorization/local";
    }

    static public class  Intermediate{
        final static String ENDPOINT_PUBLIC_AUTH = "http://public-auth";
        final public static   String ENDPOINT_PUBLIC_AUTH_REGISTER = ENDPOINT_PUBLIC_AUTH + "/user/register";
    }

    static public class  External{
        final public static String ENDPOINT_GITHUB_ORGANIZATIONS = "https://api.github.com/user/orgs";
        final public static String ENDPOINT_UI = "http://localhost:4200";
        final public static String ENDPOINT_UI_AUTH_PAGE = "/pages/auth";
    }

    static public Mono<Void> redirect(ServerHttpResponse response, URI uri) {
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().add(HttpHeaders.LOCATION, String.valueOf(uri.toString()));
        return Mono.empty();
    }

    static public class Pager {
        static public Pageable resolvePagerFromSize(Integer page, Integer pageSize, Sort sort){
            Pageable pager = Pageable.unpaged();
            if(page != null && pageSize != null && page > -1){
                pager = PageRequest.of(page, pageSize, sort);
            }
            return pager;
        }
    }

}
