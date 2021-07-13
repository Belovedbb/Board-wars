package com.board.wars.domain.marker.parts;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class StorageMarkerPart extends MarkerPart{
    public enum Type{
        FILE_SYSTEM,
        AWS,
        DRIVE,
    }

    @Id
    private String id;

    private Type type;

    private String baseLocation;

    private String[] allowedTypes;

    public String getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(String baseLocation) {
        this.baseLocation = baseLocation;
    }

    public String[] getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String[] allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String getMarkerType() {
        return "storage";
    }
}