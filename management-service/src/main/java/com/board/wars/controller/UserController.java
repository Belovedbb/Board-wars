package com.board.wars.controller;

import com.board.wars.assembler.UserModelAssembler;
import com.board.wars.payload.request.UserRequestPayload;
import com.board.wars.payload.response.GenericResponse;
import com.board.wars.payload.response.UserResponsePayload;
import com.board.wars.service.UserService;
import com.board.wars.util.Utilities;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/{auth}/management/user")
public class UserController {

    private final UserService userService;
    private final UserModelAssembler assembler;

    public UserController(UserService userService, UserModelAssembler assembler) {
        this.userService = userService;
        this.assembler = assembler;
    }

    @GetMapping
    public Mono<CollectionModel<EntityModel<GenericResponse<UserResponsePayload>>>> getUsers(@RequestParam(required = false) Boolean active,
                                                                                             @RequestParam(required = false) Integer page,
                                                                                             @RequestParam(required = false) Integer size,
                                                                                             ServerWebExchange exchange, Authentication authentication
                                                                                             ){
        return assembler.toCollectionModel(userService.getUsers(exchange, authentication, active, page, size), exchange);
    }

    @GetMapping(path = "/{username}")
    public Mono<EntityModel<GenericResponse<UserResponsePayload>>> getUser(@PathVariable String username, ServerWebExchange exchange,
                                                                              Authentication authentication){
        return userService.getUser(exchange, authentication, username).flatMap(response -> assembler.toModel(response, exchange));
    }

    @GetMapping(path = "/{username}/profile-picture", produces = { MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public Mono<byte[]> getProfilePicture(ServerWebExchange exchange, @PathVariable String username) {
        return userService.getProfilePicture(exchange.getAttribute("marker"), username);
    }

    @PostMapping(path = "/{username}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE ,
            produces = { MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public Mono<byte[]> updateUserPicture(@PathVariable String username, @RequestPart(value = "picture") Mono<FilePart> file, ServerWebExchange exchange,
                                          Authentication authentication){
        return userService.updateUserProfilePicture(file, exchange, authentication, username);
    }

    @GetMapping(path = "/current")
    public Mono<EntityModel<GenericResponse<UserResponsePayload>>> getCurrentUser( ServerWebExchange exchange, Authentication authentication){
        return userService.getCurrentUser(exchange, authentication).flatMap(response -> assembler.toModel(response, exchange));
    }

    @PatchMapping(path = "/{username}")
    public Mono<GenericResponse<UserResponsePayload>> updateUser(@PathVariable String auth, @PathVariable String username,
                                                                 @RequestBody UserRequestPayload userPayload,
                                                                 ServerWebExchange exchange, Authentication authentication){
        return userService.updateUser(username, userPayload, exchange, authentication)
                .map(resp -> Utilities.redirector(resp, exchange, "/"+Utilities.HATEOAS.injectApiVersion(exchange) + "/management/user/"+resp.getBody().getUsername()));
    }

    @DeleteMapping(path = "/{username}")
    public Mono<GenericResponse<Void>> deleteUser(@PathVariable String auth, @PathVariable String username, Authentication authentication, ServerHttpRequest request){
        return  userService.deleteUser(username, authentication, request);
    }
}
