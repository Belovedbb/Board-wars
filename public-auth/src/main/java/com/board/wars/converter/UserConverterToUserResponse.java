package com.board.wars.converter;

import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.payload.UserResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

public class UserConverterToUserResponse implements Converter<User, UserResponse> {

    @Override
    public UserResponse convert(User source) {
        Assert.notNull(source, "user source cannot be null");
        UserResponse response = new UserResponse();
        response.setFirstName(source.getFirstName());
        response.setLastName(source.getLastName());
        response.setEmail(source.getEmail());
        response.setEnabled(source.isEnabled());
        response.setExpired(source.isExpired());
        response.setLocked(source.isLocked());
        response.setFullyLocal(source.isFullyLocal());
        response.setForeignToken(source.getForeignToken());
        response.setTimeCreated(source.getTimeCreated());
        response.setPictureUrl(source.getPictureUrl());
        response.setPhoneNumber(source.getPhoneNumber());
        response.setManagementLinked(source.isManagementLinked());
        response.setRoles(source.getRoles().stream().map(Role::getRole).collect(Collectors.joining(", ")));
        return response;
    }

}
