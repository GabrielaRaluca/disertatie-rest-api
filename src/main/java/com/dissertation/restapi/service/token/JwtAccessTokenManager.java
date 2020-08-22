package com.dissertation.restapi.service.token;

import com.dissertation.restapi.exception.BadRequestException;
import com.dissertation.restapi.exception.JwtException;
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
    private final int tokenExpirationMinutes;

    private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenManager.class);

    public JwtAccessTokenManager(final String encryptionKey, final String googleClientId, final int tokenExpirationMinutes){
        this.encryptionKey = encryptionKey;
        this.googleClientId = googleClientId;
        this.tokenExpirationMinutes = tokenExpirationMinutes;
    }

    public String createAccessToken(User user){
        if(user == null){
            throw new BadRequestException("Could not retrieve user information out of the Google token!");
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("clientId", googleClientId);
            claims.put("email", user.getEmail());

            DateTime now = new DateTime(DateTimeZone.UTC);

            Date expirationDate = now.plusMinutes(tokenExpirationMinutes).toDate();

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
            throw new JwtException("Could not create access token for " + user.toString());
        }
    }

    public User extractAccessToken(String accessToken){
        try{
            Jws<Claims> claimsJws = Jwts.parser()
                    .setSigningKey(encryptionKey)
                    .parseClaimsJws(accessToken);

            Claims claims = claimsJws.getBody();

            User user = User.builder()
                    .email(claims.get("email", String.class))
                    .build();

            return user;

        }catch(Exception e){
            LOG.error("Could not decode jwt token! " + e);
            throw new JwtException("Could not decode jwt token!");
        }
    }
}
