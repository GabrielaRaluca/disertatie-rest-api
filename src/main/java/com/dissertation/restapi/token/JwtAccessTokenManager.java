package com.dissertation.restapi.token;

import com.dissertation.restapi.model.User;
import io.jsonwebtoken.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JwtAccessTokenManager {
    private final String encryptionKey;
    private final String googleClientId;
    private final int defaultExpirationSeconds;

    private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenManager.class);

    public JwtAccessTokenManager(final String encryptionKey, final String googleClientId, final int defaultExpirationSeconds){
        this.encryptionKey = encryptionKey;
        this.googleClientId = googleClientId;
        this.defaultExpirationSeconds = defaultExpirationSeconds;
    }

    public String createAccessToken(User user){

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("clientId", googleClientId);
            claims.put("email", user.getEmail());

            DateTime now = new DateTime(DateTimeZone.UTC);

            Date expirationDate = now.plusMinutes(defaultExpirationSeconds).toDate();

            return Jwts.builder()
                    .setClaims(claims)
                    .setId(UUID.randomUUID().toString())
                    .setIssuer("TravelBuddy")
                    .setExpiration(expirationDate)
                    .signWith(SignatureAlgorithm.HS256, encryptionKey)
                    .compact();
        }
        catch(Exception e){
            LOG.error("Could not create access token for user: " + user.toString() + ". Exception: " + e);
            return null;
        }
    }

    public User extractAccessToken(String accessToken){
        try{
            String unsignedJwt = accessToken.substring(accessToken.indexOf("."), accessToken.length());
            Jwt<Header, Claims> jwt = Jwts.parser().parse(accessToken);

            Claims claims = jwt.getBody();

            User user = User.builder()
                    .email(claims.get("email", String.class))
                    .build();

            return user;

        }catch(Exception e){
            LOG.error("Could not decode jwt token! " + e);
            return null;
        }
    }
}
