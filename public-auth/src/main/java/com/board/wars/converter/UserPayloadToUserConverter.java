package com.board.wars.converter;

import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.payload.UserPayload;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserPayloadToUserConverter implements Converter<UserPayload, User> {

    private PasswordEncoder encoder;

    public UserPayloadToUserConverter(PasswordEncoder encoder){
        this.encoder = encoder;
    }

    @Override
    public User convert(UserPayload source) {
        reMapper(source);
        return new User(source.getEmail(), source.getPassword(), defaultRoles(), source.getFirstName(),
                source.getLastName(), source.getPhoneNumber(), source.isEnabled(), source.getForeignToken(), source.isFullyLocal());
    }

    private void reMapper(UserPayload user){
        user.setPassword(encoder.encode(user.getPassword()));
        String[] names = user.getFullName().split("\\s", 2);
        List<String> nameParts = new ArrayList<>(names.length + 1);
        nameParts.addAll(Arrays.asList(names));
        nameParts.add("");
        user.setFirstName(nameParts.get(0));
        user.setLastName(nameParts.get(1));
        if(user.isFullyLocal()){
            user.setEnabled(true);
            user.setForeignToken(null);
            user.setPhoneNumber(null);
        }
    }
    
    private List<Role> defaultRoles(){
        Role role = new Role();
        role.setRole("VISITOR");
        return Collections.singletonList(role);
    }
}
