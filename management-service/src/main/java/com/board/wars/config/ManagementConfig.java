package com.board.wars.config;

import com.board.wars.domain.DataAudit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;

@Configuration
public class ManagementConfig {

    @Bean
    ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }

    @Bean
    public LoggingEventListener mongoEventListener() {
        return new LoggingEventListener();
    }

    @Bean
    public AuditorAware<Authentication> auditorAware(){
        return new DataAudit();
    }
}
