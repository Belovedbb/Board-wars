package com.board.wars.domain;

import com.board.wars.marker.global.GlobalMarker;
import com.board.wars.marker.global.RoleMarker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Role {
    private static final RoleMarker.RoleEntity[] nativeRoles;
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_VISITOR = "ROLE_VISITOR";

    private List<RoleMarker.RoleEntity> roles;
    private String lastUpdatedBy;
    private LocalDateTime modifiedAt;

    static {
        nativeRoles = new RoleMarker.RoleEntity[]{RoleHelper.createAdminRole(), RoleHelper.createVisitorRole()};
    }

    public List<RoleMarker.RoleEntity> getRoles() {
        if(roles == null) return new ArrayList<>();
        return roles;
    }

    public void setRoles(List<RoleMarker.RoleEntity> roles) {
        this.roles = roles;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public List<RoleMarker.RoleEntity> getAllRoles(GlobalMarker marker){
        List<RoleMarker.RoleEntity> roleList = new ArrayList<>();
        roleList.addAll(Arrays.asList(nativeRoles));
        roleList.addAll(Arrays.asList(marker.getRoleMarker().getRoleEntities()));
        return roleList;
    }

    static class RoleHelper{
        static RoleMarker.RoleEntity createAdminRole(){
            RoleMarker.RoleEntity role = new RoleMarker.RoleEntity();
            role.setAttribute(RoleMarker.ROLE_ATTRIBUTE.ALL);
            role.setSemantic(RoleMarker.ROLE_SEMANTIC.ALL);
            role.setPrecedence(-1000L);
            role.setName(ROLE_ADMIN);
            return role;
        }

        static RoleMarker.RoleEntity createVisitorRole(){
            RoleMarker.RoleEntity role = new RoleMarker.RoleEntity();
            role.setAttribute(RoleMarker.ROLE_ATTRIBUTE.NONE);
            role.setSemantic(RoleMarker.ROLE_SEMANTIC.VIEW);
            role.setPrecedence(-1000L);
            role.setName(ROLE_VISITOR);
            return role;
        }
    }

}
