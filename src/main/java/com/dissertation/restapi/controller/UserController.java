package com.dissertation.restapi.controller;

import com.dissertation.restapi.login.GoogleLogin;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.repository.UserRepository;
import com.dissertation.restapi.service.token.JwtAccessTokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path="/api")
public class UserController {

    private final GoogleLogin googleLogin;

    private final JwtAccessTokenManager jwtAccessTokenManager;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserController(GoogleLogin googleLogin, JwtAccessTokenManager jwtAccessTokenManager, UserRepository userRepository){
        this.googleLogin = googleLogin;
        this.jwtAccessTokenManager = jwtAccessTokenManager;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/login/google", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity login(@RequestBody MultiValueMap<String, String> formData) {
        ObjectNode responseBody = objectMapper.createObjectNode();

        if(formData == null || formData.getFirst("googleIdToken") == null) {
            responseBody.put("success", true);
            responseBody.put("message", "Missing token");
            responseBody.put("statusCode", "400");

            return ResponseEntity.badRequest().body(responseBody);
        }

        String googleIdToken = formData.getFirst("googleIdToken");
        User user = googleLogin.getUser(googleIdToken);

        String accessToken = jwtAccessTokenManager.createAccessToken(user);

        ObjectNode userData = objectMapper.createObjectNode();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("picture_url", user.getPictureUrl());
        userData.put("access_token", accessToken);

        responseBody.put("success", true);
        responseBody.set("data", userData);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping(value="/user/{id}")
    public ResponseEntity getUserById(@PathVariable("id") Long id){
        Optional<User> optionalUser = userRepository.findById(id);
        ObjectNode responseBody = objectMapper.createObjectNode();

        if (!optionalUser.isPresent()) {
            responseBody.put("success", false);
            responseBody.put("message", "No user exists with id " + id);
            responseBody.put("statusCode", 400);

            return ResponseEntity.badRequest().body(responseBody);

        } else {
            User user = optionalUser.get();
            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("picture_url", user.getPictureUrl());
            userData.put("description", user.getDescription());

            responseBody.put("success", true);
            responseBody.set("data", userData);

            return ResponseEntity.ok(responseBody);
        }
    }

    @PutMapping(value = "/user/{id}")
    ResponseEntity updateUser(@PathVariable Long id, @RequestBody User body) {
        Optional<User> optionalUser = userRepository.findById(id);
        ObjectNode responseBody = objectMapper.createObjectNode();

        if (!optionalUser.isPresent()) {
            responseBody.put("success", false);
            responseBody.put("message", "No user exists with id " + id);
            responseBody.put("statusCode", 400);

            return ResponseEntity.badRequest().body(responseBody);

        } else {
            User user = optionalUser.get();
            user.setDescription(body.getDescription());

            userRepository.save(user);

            ObjectNode userData = objectMapper.createObjectNode();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            userData.put("picture_url", user.getPictureUrl());
            userData.put("description", user.getDescription());

            responseBody.put("success", true);
            responseBody.set("data", userData);

            return ResponseEntity.ok(responseBody);
        }
    }
}
