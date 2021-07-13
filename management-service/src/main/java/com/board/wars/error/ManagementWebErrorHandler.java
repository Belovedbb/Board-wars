package com.board.wars.error;

import com.board.wars.util.Utilities;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//TODO log error
@Component
@Order(-2)
public class ManagementWebErrorHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if(! (exception instanceof ManagementResponseException)){
            exception.printStackTrace();
            exception = transformToManagementResponseException(exception);
        }
        ManagementResponseException error = (ManagementResponseException) exception;
        return handleManagementResponseException(exchange, error);
    }
    
    private Mono<Void> handleManagementResponseException(ServerWebExchange webExchange, ManagementResponseException error){
        return Mono.just(webExchange)
                .map(resp -> setMetaData(resp, error))
                .flatMap(exchange -> setBody(exchange, error.getBody()));
    }
    
    private ServerWebExchange setMetaData(ServerWebExchange exchange, final ManagementResponseException error){
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(error.getStatusCode()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange;
    }
    
    private Mono<Void> setBody(ServerWebExchange exchange, ErrorResponsePayload payload){
        return exchange.getResponse().writeWith(getSerializedBody(exchange, payload));
    }

    private Flux<DataBuffer> getSerializedBody(ServerWebExchange exchange, ErrorResponsePayload payload){
        ServerHttpResponse response = exchange.getResponse();
        Jackson2JsonEncoder jsonEncoder = new Jackson2JsonEncoder();
        jsonEncoder.getObjectMapper().findAndRegisterModules();
        jsonEncoder.getObjectMapper().configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return jsonEncoder.encode(Mono.just(payload), response.bufferFactory(),
                ResolvableType.forInstance(payload), MediaType.APPLICATION_JSON,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.getLogPrefix()));
    }

    private ManagementResponseException transformToManagementResponseException(Throwable exception){
        return new Utilities.ManagementErrorBuilder()
                .addErrorStatus((long) HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), null)
                .build(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

}
