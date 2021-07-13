package com.board.wars.domain.marker.parts;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

//organization custom role
@Document
public class RoleMarkerPart extends MarkerPart{
    @Id
    private String id;

    private RoleEntity[] roleEntities;
    private boolean activate;

    public RoleEntity[] getRoleEntities() {
        return roleEntities;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public void setRoleEntities(RoleEntity[] roleEntities) {
        this.roleEntities = roleEntities;
    }

    public static class RoleEntity{
        private ROLE_ATTRIBUTE attribute;
        private ROLE_SEMANTIC semantic;
        private Long precedence;
        private String name;

        public ROLE_ATTRIBUTE getAttribute() {
            return attribute;
        }

        public void setAttribute(ROLE_ATTRIBUTE attribute) {
            this.attribute = attribute;
        }

        public ROLE_SEMANTIC getSemantic() {
            return semantic;
        }

        public void setSemantic(ROLE_SEMANTIC semantic) {
            this.semantic = semantic;
        }

        public Long getPrecedence() {
            return precedence;
        }

        public void setPrecedence(Long precedence) {
            this.precedence = precedence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public enum ROLE_ATTRIBUTE{
        BOARD,
        PROJECT,
        TASK
    }

    public enum ROLE_SEMANTIC{
        CREATE,
        VIEW,
        MODIFY
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getMarkerType() {
        return "role";
    }
}
