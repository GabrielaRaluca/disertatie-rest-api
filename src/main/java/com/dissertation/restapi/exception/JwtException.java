package com.dissertation.restapi.exception;

public class JwtException extends BadRequestException {
    public JwtException(String message) {
        super("INVALID_JWT", message);
    }
}
