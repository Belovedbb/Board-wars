package com.board.wars.service;

import com.board.wars.converter.UserUpdatePayloadToUserConverter;
import com.board.wars.domain.Role;
import com.board.wars.domain.User;
import com.board.wars.payload.UserPayload;
import com.board.wars.payload.UserResponse;
import com.board.wars.payload.UserUpdatePayload;
import com.board.wars.store.UserRepository;
import com.board.wars.util.Constant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
public class UserService implements UserDetailsService {

    @Value("${service.homepage}")
    String serviceHomePage;
    private final PasswordEncoder passwordEncoder;
    private final UserUpdatePayloadToUserConverter userUpdateConverter;

    private final UserRepository userRepository;
    private final ConversionService conversionService;

    public UserService(UserRepository userRepository, ConversionService conversionService, PasswordEncoder passwordEncoder, UserUpdatePayloadToUserConverter userUpdateConverter) {
        this.userRepository = userRepository;
        this.conversionService = conversionService;
        this.passwordEncoder = passwordEncoder;
        this.userUpdateConverter = userUpdateConverter;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user does not exist"));
        UserBuilder userDetailsBuilder = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .accountExpired(user.isExpired())
                .accountLocked(user.isLocked())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .authorities(user.getRoles().stream().map(Role::getRole).toArray(String[]::new));
        return userDetailsBuilder.build();
    }

    public Optional<UserResponse> getUser(String email) {
        Optional<UserResponse> response = Optional.empty();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user does not exist"));
        if (conversionService.canConvert(User.class, UserResponse.class)){
            response = Optional.ofNullable(conversionService.convert(user, UserResponse.class));
        }
        return response;
    }

    public List<UserResponse> getUsers(Boolean active) {
        List<User> users = active == null ? userRepository.findAll() : userRepository.findAllByExpired(active);
        return users.stream()
                .map(user -> conversionService.convert(user, UserResponse.class)).collect(Collectors.toList());
    }

    public List<UserResponse> getManagementUsers(boolean linked) {
        return userRepository.findUsersByManagementLinkedEquals(linked).stream()
                .map(user -> conversionService.convert(user, UserResponse.class)).collect(Collectors.toList());
    }

    public User saveUser(UserPayload userPayload) {
        User user = conversionService.convert(userPayload, User.class);
        return userRepository.save(user);
    }

    public UserResponse getRedirectedUserResponse(User user, HttpServletResponse response){
        String finalUrl = StringUtils.hasText(serviceHomePage) ? response.encodeURL(serviceHomePage) : "/";
        response.addHeader(Constant.REDIRECT_KEY, finalUrl);
        return conversionService.convert(user, UserResponse.class);
    }

    public Optional<UserResponse> updateUser(UserUpdatePayload userUpdatePayload, String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user does not exist"));
        if (user.isManagementLinked()){
            userUpdatePayload.setManagementLinked(true);
        }else if (userUpdatePayload.getManagementLinked() == null || !userUpdatePayload.getManagementLinked()){
            userUpdatePayload.setManagementLinked(user.isManagementLinked());
        }
        User updatedUser = userRepository.save(userUpdateConverter.convert(userUpdatePayload, user));
        return Optional.ofNullable(conversionService.convert(updatedUser, UserResponse.class));
    }

    @Deprecated
    public boolean validatePassword(String email, String password){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("user does not exist"));
        return passwordEncoder.matches(password, user.getPassword());
    }

}
