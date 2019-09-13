package com.dissertation.restapi.exception;

public class EntityNotFoundException extends HttpException {

    public EntityNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
