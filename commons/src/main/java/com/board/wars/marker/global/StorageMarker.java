package com.board.wars.marker.global;

public class StorageMarker {

    private String id;

    private Type type;

    private String baseLocation;

    private String[] allowedTypes;

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

    public enum Type{
        FILESYSTEM,
        GOOGLE_CLOUD,
        AMAZON_AWS
    }
}
