package com.dissertation.restapi.service.token;

import com.dissertation.restapi.model.User;
import com.dissertation.restapi.utils.ServletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * Created by raluca on 25.06.2017.
 */
public class TokenInterceptor extends HandlerInterceptorAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TokenInterceptor.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String MISSING_TOKEN = "Missing security token";

    private final JwtAccessTokenManager accessTokenManager;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TokenInterceptor(JwtAccessTokenManager accessTokenManager) {
        this.accessTokenManager = accessTokenManager;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws Exception {

        String accessTokenString = request.getHeader(AUTHORIZATION_HEADER);

        if (Objects.isNull(accessTokenString) || accessTokenString.isEmpty()) {

            LOG.warn(MISSING_TOKEN);

            ObjectNode mainResponse = MAPPER.createObjectNode();
            mainResponse.put("success", false);
            mainResponse.put("message", MISSING_TOKEN);
            mainResponse.put("statusCode", "UNAUTHORIZED");

            ServletUtils.writeJsonResponse(response, mainResponse, 401);

            return false;
        }

        try {

            User user = accessTokenManager.extractAccessToken(accessTokenString);
            // request.setAttribute("accessToken", user);

            return true;

        } catch (Exception e) {

            LOG.warn(e.getMessage());

            ObjectNode mainResponse = MAPPER.createObjectNode();
            mainResponse.put("success", false);
            mainResponse.put("error", e.getMessage());

            ServletUtils.writeJsonResponse(response, mainResponse, 401);

            return false;

        }

    }
}
