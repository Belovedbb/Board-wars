package com.board.wars.domain;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class DataAudit implements AuditorAware<Authentication> {
    @Override
    public Optional<Authentication> getCurrentAuditor() {
        return Optional.of(SecurityContextHolder.getContext().getAuthentication());
    }
}
