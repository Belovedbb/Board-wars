package com.board.wars.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.context.SecurityContextImpl;

@Document(value = "ContextUser")
public class ContextUser {
    public ContextUser(){}

    @PersistenceConstructor
    public ContextUser(String id, SecurityContextImpl context) {
        this.id = id;
        this.context = context;
    }

    @Id
    private String id;

    private SecurityContextImpl context;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecurityContextImpl getContext() {
        return context;
    }

    public void setContext(SecurityContextImpl context) {
        this.context = context;
    }


}
