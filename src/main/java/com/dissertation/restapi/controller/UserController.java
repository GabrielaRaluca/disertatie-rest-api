package com.dissertation.restapi.controller;


import com.dissertation.restapi.exception.BadRequestException;
import com.dissertation.restapi.exception.EntityNotFoundException;
import com.dissertation.restapi.login.GoogleLogin;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.repository.UserRepository;
import com.dissertation.restapi.service.token.JwtAccessTokenManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import javax.transaction.Transactional;

@Transactional
@CrossOrigin(origins = "*")
@RestController
//@RequestMapping(path="/users")
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
    public ResponseEntity login(@RequestBody MultiValueMap<String, String> formData) throws JsonProcessingException {
        if(formData == null || formData.getFirst("googleIdToken") == null){
            throw new BadRequestException("Requested body not present!");
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

        ObjectNode responseBody = objectMapper.createObjectNode();
        responseBody.put("success", true);
        responseBody.set("data", userData);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping(value="/user/{id}")
    public ResponseEntity getUserById(@PathVariable("id") Long id){
        User user = userRepository.findById(id).orElseThrow(() ->
            new EntityNotFoundException("No such user exists!"));

        ObjectNode userData = objectMapper.createObjectNode();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("picture_url", user.getPictureUrl());

        ObjectNode responseBody = objectMapper.createObjectNode();
        responseBody.put("success", true);
        responseBody.set("data", userData);

        return ResponseEntity.ok(responseBody);
    }
}
