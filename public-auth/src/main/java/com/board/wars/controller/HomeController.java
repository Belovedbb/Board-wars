package com.board.wars.controller;

import com.board.wars.domain.User;
import com.board.wars.payload.UserPayload;
import com.board.wars.payload.UserResponse;
import com.board.wars.payload.UserUpdatePayload;
import com.board.wars.service.UserService;
import com.board.wars.util.Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/user", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class HomeController {

    final private UserService service;

    public HomeController(UserService service) {
        this.service = service;
    }

    //TODO make user with email unique in db
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserPayload userPayload, HttpServletResponse response){
        User savedUser = service.saveUser(userPayload);
        if(savedUser != null){
            return new ResponseEntity<>(service.getRedirectedUserResponse(savedUser, response), HttpStatus.CREATED);
        }
        return new ResponseEntity<>(new UserResponse(Constant.FAILURE_STATE_MESSAGE), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/linked")
    public ResponseEntity<List<UserResponse>> filterManagementNotLinkedUser(@RequestParam boolean linked) {
        return new ResponseEntity<>(service.getManagementUsers(linked), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false) Boolean isActive) {
        return new ResponseEntity<>(service.getUsers(isActive), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{user}")
    public ResponseEntity<Optional<UserResponse>> getUserByIdentity(@PathVariable("user") String email) {
        Optional<UserResponse> user = service.getUser(email);
        if(user.isEmpty()){
            return new ResponseEntity<>(Optional.of(new UserResponse(Constant.FAILURE_STATE_MESSAGE)), HttpStatus.BAD_REQUEST) ;
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{user}")
    public ResponseEntity<Optional<UserResponse>> updateUser(@PathVariable("user") String email, @RequestBody UserUpdatePayload userPayload) {
        Optional<UserResponse> user = service.updateUser(userPayload, email);
        if(user.isEmpty()){
            return new ResponseEntity<>(Optional.of(new UserResponse(Constant.FAILURE_STATE_MESSAGE)), HttpStatus.BAD_REQUEST) ;
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
