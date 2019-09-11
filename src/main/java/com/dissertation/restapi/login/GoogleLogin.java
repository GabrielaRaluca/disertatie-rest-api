package com.dissertation.restapi.login;

import com.dissertation.restapi.exception.BadRequestException;
import com.dissertation.restapi.exception.JwtException;
import com.dissertation.restapi.model.User;
import com.dissertation.restapi.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleLogin {

    private final static Logger LOG = LoggerFactory.getLogger(GoogleLogin.class);

    private String clientId;
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    private JacksonFactory jacksonFactory = new JacksonFactory();
    private static final HttpTransport transport = new NetHttpTransport();
    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public GoogleLogin(String clientId){
        this.clientId = clientId;

        googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport,jacksonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public User getUser(String tokenId){
        if(tokenId.isEmpty()){
            LOG.warn("Empty token id!");
            throw new BadRequestException("Empty token id!");
        }

        try {
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(tokenId);

            if(googleIdToken != null){
                GoogleIdToken.Payload payload = googleIdToken.getPayload();

                String email = payload.getEmail();
                String givenName = (String)payload.get("name");
                String pictureUrl = (String)payload.get("picture");

                User user = this.userRepository.findByEmail(email).orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(givenName)
                            .pictureUrl(pictureUrl)
                            .build();

                    userRepository.save(newUser);
                    return newUser;
                });

                return user;
            }
            else{
                LOG.warn("Invalid tokenId " + tokenId);
                throw new JwtException("Invalid token id!");
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            LOG.warn("Could not verify tokenId " + tokenId);
            throw new JwtException("Could not verify token id!");
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warn("Could not verify tokenId " + tokenId);
            throw new JwtException("Could not verify token id!");
        }
    }
}
