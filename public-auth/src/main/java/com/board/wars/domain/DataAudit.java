package com.board.wars.domain;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class DataAudit implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(auditorHandler(SecurityContextHolder.getContext()));
    }


    private String auditorHandler(SecurityContext context){
        if(context != null ){
            if(context.getAuthentication() != null){
                return context.getAuthentication().getName();
            }else{
                return "SYSTEM";
            }
        }else{
            return "UNKNOWN";
        }
    }
}
