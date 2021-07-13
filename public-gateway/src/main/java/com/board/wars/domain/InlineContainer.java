package com.board.wars.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "InlineContainer")
public  class InlineContainer {
    @Id
    private String id;
    private String containerId;
    private LocalDateTime validUntil;
    private ContextUser contextUser;

    public InlineContainer(){}

    @PersistenceConstructor
    public InlineContainer(String id, String containerId, LocalDateTime validUntil, ContextUser contextUser) {
        this.id = id;
        this.containerId = containerId;
        this.validUntil = validUntil;
        this.contextUser = contextUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public ContextUser getContextUser() {
        return contextUser;
    }

    public void setContextUser(ContextUser contextUser) {
        this.contextUser = contextUser;
    }
}