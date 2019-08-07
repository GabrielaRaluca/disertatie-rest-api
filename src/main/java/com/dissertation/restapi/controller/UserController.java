package com.dissertation.restapi.controller;


import com.dissertation.restapi.login.GoogleLogin;
import com.dissertation.restapi.model.User;
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
    GoogleLogin googleLogin;

    @PostMapping(value = "/login/google", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity login(@RequestBody MultiValueMap<String, String> formData){
        String googleIdToken = formData.getFirst("googleIdToken");
        User user = googleLogin.getUser(googleIdToken);

        return ResponseEntity.ok(user);
    }
}
