package com.board.wars.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.savedrequest.CookieServerRequestCache;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;

@Configuration
public class PublicConfiguration {

    @Bean
    ServerRequestCache requestCache(){
        return new CookieServerRequestCache();
    }


}
