package com.dissertation.restapi;

import com.dissertation.restapi.login.GoogleLogin;
import com.dissertation.restapi.service.token.JwtAccessTokenManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public GoogleLogin getGoogleLogin(@Value("${google.clientId}") String clientId){
        return new GoogleLogin(clientId);
    }

    @Bean
    public JwtAccessTokenManager jwtAccessTokenManager(@Value("${google.clientId}") String clientId,
                                                       @Value("${token.encryptionKey}") String encryptionKey,
                                                       @Value("${token.expirationMinutes}") int tokenExpirationMinutes){
        return new JwtAccessTokenManager(encryptionKey, clientId, tokenExpirationMinutes);
    }
}
