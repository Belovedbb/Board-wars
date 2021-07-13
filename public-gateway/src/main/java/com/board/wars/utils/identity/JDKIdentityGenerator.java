package com.board.wars.utils.identity;

import org.springframework.util.AlternativeJdkIdGenerator;

public class JDKIdentityGenerator extends AlternativeJdkIdGenerator implements IdentityGenerator{
    @Override
    public String generate() {
        return this.generateId().toString();
    }

}
