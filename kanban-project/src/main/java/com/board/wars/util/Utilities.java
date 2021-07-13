package com.board.wars.util;

import com.board.wars.error.ErrorResponseBody;
import com.board.wars.error.ErrorResponsePayload;
import com.board.wars.error.KanbanResponseException;
import com.board.wars.payload.response.GenericResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

public class Utilities {

    public static final String MY_NAME = "KanbanProject";
    public final static String PROJECT_CODE_KEY = "project_code";
    public final static String COLUMN_NAME_KEY = "column_name";
    public final static String TASK_ID_KEY = "task_id";
    public final static String SUB_TASK_ID_KEY = "sub_task_id";
    public final static String TASK_COMMENT_ID_KEY = "task_comment_id";

    public static final String KANBAN = "kanban";

    static public <T> T redirector(T instance, ServerWebExchange exchange, String url) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().add(HttpHeaders.LOCATION, url);
        return instance;
    }

    static public class HATEOAS{
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

    static public class KanbanErrorBuilder{
        private List<ErrorResponseBody.ErrorStatus> statusHolder = new ArrayList<>();

        public KanbanErrorBuilder addErrorStatus(Long code, String description, String parameter){
            statusHolder.add( new ErrorResponseBody.ErrorStatus(code, description, parameter));
            return this;
        }

        public KanbanResponseException build(HttpStatus status, @Nullable String message){
            ErrorResponsePayload payload = new ErrorResponsePayload(new ErrorResponseBody(statusHolder));
            return new KanbanResponseException(message, status.value(), payload);
        }

        public static <T> Mono<GenericResponse<T>> emptyResponseWithThrow(boolean withError, HttpStatus status){
            Mono<GenericResponse<T>> response = Mono.just(new GenericResponse<>(null));
            if(withError) {
                HttpStatus internalStatus = status != null ? status : HttpStatus.BAD_REQUEST;
                response = Mono.error(new Utilities.KanbanErrorBuilder()
                        .addErrorStatus((long) internalStatus.value(), internalStatus.getReasonPhrase(), "")
                        .build(internalStatus, null));
            }
            return response;
        }
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

    public enum PersistentLog {
        CREATE,
        UPDATE,
        DELETE,
        LOG
    }
}
