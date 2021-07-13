package com.board.wars.config.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private String interfaceHost;

    @NotBlank
    private String markerTokenHash;

    @NotBlank
    private String markerTokenHashClaim;

    @NotBlank
    private String markerTokenHashKey;

    public String getInterfaceHost() {
        return interfaceHost;
    }

    public void setInterfaceHost(String interfaceHost) {
        this.interfaceHost = interfaceHost;
    }

    public String getMarkerTokenHash() {
        return markerTokenHash;
    }

    public void setMarkerTokenHash(String markerTokenHash) {
        this.markerTokenHash = markerTokenHash;
    }

    public String getMarkerTokenHashClaim() {
        return markerTokenHashClaim;
    }

    public void setMarkerTokenHashClaim(String markerTokenHashClaim) {
        this.markerTokenHashClaim = markerTokenHashClaim;
    }

    public String getMarkerTokenHashKey() {
        return markerTokenHashKey;
    }

    public void setMarkerTokenHashKey(String markerTokenHashKey) {
        this.markerTokenHashKey = markerTokenHashKey;
    }
}
