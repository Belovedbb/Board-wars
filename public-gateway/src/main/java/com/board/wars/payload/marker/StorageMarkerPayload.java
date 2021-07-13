package com.board.wars.payload.marker;

public class StorageMarkerPayload {
    private StorageType storageType;
    private String baseLocation;//for read and write
    private String[] allowedTypes;

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
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

    public enum StorageType{
        FILESYSTEM,
        GOOGLE_CLOUD,
        AMAZON_AWS
    }
}
