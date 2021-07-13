package com.board.wars.config.auth;

import org.springframework.security.web.server.header.ServerHttpHeadersWriter;
import org.springframework.security.web.server.header.StaticServerHttpHeadersWriter;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;


public final class ClearSiteDataAggregateHttpHeadersWriter implements ServerHttpHeadersWriter {

    public static final String CLEAR_SITE_DATA_HEADER = "Clear-Site-Data";

    private final StaticServerHttpHeadersWriter headerWriterDelegate;


    public ClearSiteDataAggregateHttpHeadersWriter(Directive... directives) {
        Assert.notEmpty(directives, "directives cannot be empty or null");
        this.headerWriterDelegate = StaticServerHttpHeadersWriter.builder()
                .header(CLEAR_SITE_DATA_HEADER, transformToHeaderValue(directives)).build();
    }

    @Override
    public Mono<Void> writeHttpHeaders(ServerWebExchange exchange) {
        return this.headerWriterDelegate.writeHttpHeaders(exchange);
    }

    public enum Directive {

        CACHE("cache"),

        COOKIES("cookies"),

        STORAGE("storage"),

        EXECUTION_CONTEXTS("executionContexts"),

        ALL("*");

        private final String headerValue;

        Directive(String headerValue) {
            this.headerValue = "\"" + headerValue + "\"";
        }

        public String getHeaderValue() {
            return this.headerValue;
        }

    }

    private String transformToHeaderValue(Directive... directives) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < directives.length - 1; i++) {
            sb.append(directives[i].headerValue).append(", ");
        }
        sb.append(directives[directives.length - 1].headerValue);
        return sb.toString();
    }

}
