package com.board.wars.util;

import com.board.wars.controller.UserController;
import com.board.wars.error.ErrorResponseBody;
import com.board.wars.error.ErrorResponsePayload;
import com.board.wars.error.ManagementResponseException;
import com.board.wars.payload.response.GenericResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

public class Utilities {

    public static final String MY_NAME = "ManagementService";
    public static final int PICTURE_LENGTH = 150;
    public static final String MANAGEMENT = "Management";

    static public <T> T redirector(T instance, ServerWebExchange exchange, String url) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().add(HttpHeaders.LOCATION, url);
        return instance;
    }

    static public <T> T setAndGet(T prop, Consumer<T> consumer){
        consumer.accept(prop);
        return prop;
    }

    static public Mono<String> getApiProfileLink(ServerWebExchange webExchange, String name){
        return linkTo(Utilities.HATEOAS.getInjectedMethod(UserController.class, webExchange).getProfilePicture( webExchange,name), webExchange)
                .withSelfRel()
                .toMono().map(Link::getHref);
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }


    static public String resolveRoute(String link, String... params) {
        for (int i = 0; i < params.length; i++) {
            String factor = "{" + i + "}";
            link = link.replace(factor, params[i]);
        }
        return link;
    }

    static public class Pager{
        static public Pageable resolvePagerFromSize(Integer page, Integer pageSize, Sort sort){
            Pageable pager = Pageable.unpaged();
            if(page != null && pageSize != null && page > -1){
                pager = PageRequest.of(page, pageSize, sort);
            }
            return pager;
        }
    }

    static public class ManagementErrorBuilder{
        private List<ErrorResponseBody.ErrorStatus> statusHolder = new ArrayList<>();

        public ManagementErrorBuilder addErrorStatus(Long code, String description, String parameter){
            statusHolder.add( new ErrorResponseBody.ErrorStatus(code, description, parameter));
            return this;
        }

        public ManagementResponseException build(HttpStatus status, @Nullable String message){
            ErrorResponsePayload payload = new ErrorResponsePayload(new ErrorResponseBody(statusHolder));
            return new ManagementResponseException(message, status.value(), payload);
        }

        public static <T> Mono<GenericResponse<T>> emptyResponseWithThrow(boolean withError, HttpStatus status){
            Mono<GenericResponse<T>> response = Mono.just(new GenericResponse<>(null));
            if(withError) {
                HttpStatus internalStatus = status != null ? status : HttpStatus.BAD_REQUEST;
                response = Mono.error(new Utilities.ManagementErrorBuilder()
                        .addErrorStatus((long) internalStatus.value(), internalStatus.getReasonPhrase(), "")
                        .build(internalStatus, null));
            }
            return response;
        }
    }

    static public class HATEOAS {
        static final private String API_VERSION_KEY = "api_version_key";

        public static  <T> T getInjectedMethod(Class<T> klazz, ServerWebExchange exchange){
            return methodOn(klazz, Utilities.HATEOAS.injectApiVersion(exchange));
        }

        public static String injectApiVersion(ServerWebExchange exchange){
            String apiVersionValue = exchange.getRequest().getHeaders().toSingleValueMap().getOrDefault(API_VERSION_KEY, null);
            Assert.notNull(apiVersionValue, "request must contain api version value");
            String trimmedPath = trimPath(apiVersionValue.getBytes());
            return StringUtils.hasText(trimmedPath) ? trimmedPath : null;
        }

        private static String trimPath(byte[] value) {
            int len = value.length;
            int st = 0;
            while ((st < len) && ((value[st] & 0xff) <= '/')) {
                st++;
            }
            while ((st < len) && ((value[len - 1] & 0xff) <= '/')) {
                len--;
            }
            return ((st > 0) || (len < value.length)) ?
                    new String(value, st, len - st) : null;
        }

    }

    public enum PersistentLog {
        CREATE,
        UPDATE,
        DELETE,
        LOG
    }

}
