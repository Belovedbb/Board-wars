package com.board.wars.mapper;

import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.AuthUserResponse;
import com.board.wars.payload.response.UserResponsePayload;
import org.mapstruct.*;

@Mapper
public interface UserMapper {
    String role = "java(user.getRole() != null && user.getRole().getRoles() != null && !user.getRole().getRoles().isEmpty()" +
            " ? user.getRole().getRoles().get(0).getName() : null)";

    @Mapping(target = "username", source = "email")
    @Mapping(target = "authTimeCreated", source = "timeCreated")
    User convertAuthUserToManagementUser(AuthUserResponse authUser);

    @Mapping(target = "role", expression = role)
    @Mapping(target = "otherNames", source = "lastName")
    UserResponsePayload mapUserDomainToResponsePayload(User user);

    @Mapping(target = "lastName", source = "otherNames")
    @Mapping(target = "role", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    User mapUserRequestPayloadToUser(@MappingTarget User savedUser, UserRequestPayload payload);

    /*@Mapping(target = "organization", qualifiedByName = "organizationSubMapper", source = "credentials")
    Role convertPayloadRoleToRole(String role);

    @Named("rolePayloadToRole")
    default Role mapRoleAttributes(String role) {
        if(Role.ROLE_ADMIN.equals(role)){

        }

    } */
}
