package com.dissertation.restapi.exception;

public class BadRequestException extends HttpException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

    protected BadRequestException(String statusCode, String message){
        super(statusCode, message);
    }
}
