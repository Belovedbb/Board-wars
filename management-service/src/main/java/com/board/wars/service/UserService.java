package com.board.wars.service;

import com.board.wars.domain.User;
import com.board.wars.marker.global.GlobalMarker;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.AuthUserResponse;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.Operation;
import com.board.wars.payload.response.UserResponsePayload;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {

    Mono<GenericResponse<UserResponsePayload>> getUser(ServerWebExchange webExchange, Authentication authentication, String username);

    Mono<GenericResponse<UserResponsePayload>> getCurrentUser(ServerWebExchange webExchange, Authentication authentication);

    Flux<GenericResponse<UserResponsePayload>> getUsers(ServerWebExchange webExchange, Authentication authentication,Boolean active, Integer page, Integer pageSize);

    Mono<GenericResponse<UserResponsePayload>> updateUser(String username, UserRequestPayload userPayload, ServerWebExchange exchange, Authentication authentication);

    Mono<GenericResponse<Void>> deleteUser(String username, Authentication authentication, ServerHttpRequest request);

    Mono<byte[]> getProfilePicture(GlobalMarker marker, String name);

    Mono<byte[]> updateUserProfilePicture(Mono<FilePart> file, ServerWebExchange exchange, Authentication authentication, String username);

    User updateUserRole(User savedUser, GlobalMarker marker, UserRequestPayload payload, String self);

    Mono<User> updateAuthServerUser(User user, ServerHttpRequest request, AuthUserResponse authUserResponse);

    Mono<Operation> getUserOperation(Authentication authentication, String subject);

    Mono<Boolean> isUserNamesExist(List<String> userNames);

    Flux<User> getAllUsersInList(List<String> userNames);

}
