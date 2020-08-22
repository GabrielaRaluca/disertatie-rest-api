package com.dissertation.restapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ServletUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ServletUtils.class);

    private ServletUtils() {

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();


    public static void writeJsonResponse(final HttpServletResponse response,
                                         final Object obj,
                                         final int httpStatusCode) throws IOException {
        response.setStatus(httpStatusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = response.getWriter();
        try {
            MAPPER.writeValue(writer, obj);
        } catch (Exception e) {
            LOG.error("Error writing JSON!", e);
        }
    }
}
