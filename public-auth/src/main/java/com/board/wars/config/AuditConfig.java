package com.board.wars.config;

import com.board.wars.domain.DataAudit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@Configuration
public class AuditConfig {
    @Bean
    public AuditorAware<String> auditorAware(){
        return new DataAudit();
    }
}
