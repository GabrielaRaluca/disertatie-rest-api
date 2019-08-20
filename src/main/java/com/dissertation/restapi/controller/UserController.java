package com.dissertation.restapi.controller;


import com.dissertation.restapi.exception.BadRequestException;
import com.dissertation.restapi.login.GoogleLogin;
import com.dissertation.restapi.model.User;
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

    @Autowired
    private GoogleLogin googleLogin;

    @Autowired
    private JwtAccessTokenManager jwtAccessTokenManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/login/google", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity login(@RequestBody MultiValueMap<String, String> formData) throws JsonProcessingException {
        if(formData == null || formData.getFirst("googleIdToken") == null){
            throw new BadRequestException("Requested body not present!");
        }

        String googleIdToken = formData.getFirst("googleIdToken");
        User user = googleLogin.getUser(googleIdToken);

        String accessToken = jwtAccessTokenManager.createAccessToken(user);

        ObjectNode responseBody = objectMapper.createObjectNode();
        responseBody.put("email", user.getEmail());
        responseBody.put("name", user.getName());
        responseBody.put("picture_url", user.getPictureUrl());
        responseBody.put("access_token", accessToken);

        return ResponseEntity.ok(responseBody);
    }
}
