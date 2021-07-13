package com.board.wars.converter;


import com.board.wars.domain.User;
import com.board.wars.payload.UserUpdatePayload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserUpdatePayloadToUserConverter  {

    private PasswordEncoder encoder;

    public UserUpdatePayloadToUserConverter(PasswordEncoder encoder){
        this.encoder = encoder;
    }

    public User convert(UserUpdatePayload source, User target) {
        if (source.getExpired() != null) target.setExpired(source.getExpired());
        if (StringUtils.hasText(source.getFirstName())) target.setFirstName(source.getFirstName());
        if (StringUtils.hasText(source.getLastName())) target.setLastName(source.getLastName());
        if (source.getManagementLinked() != null) target.setManagementLinked(source.getManagementLinked());
        if (StringUtils.hasText(source.getPhoneNumber())) target.setPhoneNumber(source.getPhoneNumber());
        if (StringUtils.hasText(source.getPassword())) target.setPassword(encoder.encode(source.getPassword()));
        if (StringUtils.hasText(source.getPictureUrl())) target.setPictureUrl(source.getPictureUrl());

        return target;
    }


}
