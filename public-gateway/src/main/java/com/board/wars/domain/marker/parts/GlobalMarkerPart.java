package com.board.wars.domain.marker.parts;

import com.board.wars.domain.marker.OrganizationDetail;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//organization details
@Document
public class GlobalMarkerPart extends MarkerPart{
    @Id
    private String id;

    private String organizationName;

    private String registrarEmail;

    private String registrarName;

    private OrganizationDetail organization;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getRegistrarName() {
        return registrarName;
    }

    public void setRegistrarName(String registrarName) {
        this.registrarName = registrarName;
    }

    @Override
    public String getMarkerType() {
        return "global";
    }

    public OrganizationDetail getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDetail organization) {
        this.organization = organization;
    }

    public String getRegistrarEmail() {
        return registrarEmail;
    }

    public void setRegistrarEmail(String registrarEmail) {
        this.registrarEmail = registrarEmail;
    }
}