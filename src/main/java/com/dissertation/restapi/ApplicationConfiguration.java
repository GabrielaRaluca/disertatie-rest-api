package com.dissertation.restapi;

import com.dissertation.restapi.login.GoogleLogin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public GoogleLogin getGoogleLogin(@Value("${google.clientId}") String clientId){
        return new GoogleLogin(clientId);
    }
}
