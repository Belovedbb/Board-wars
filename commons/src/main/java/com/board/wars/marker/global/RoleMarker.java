package com.board.wars.marker.global;

public class RoleMarker {

    private String id;

    private RoleEntity[] roleEntities;

    private boolean activate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
        TASK,
        ALL,
        NONE
    }

    public enum ROLE_SEMANTIC{
        CREATE,
        VIEW,
        MODIFY,
        ALL
    }
}
