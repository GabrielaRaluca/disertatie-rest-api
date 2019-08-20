package com.dissertation.restapi.exception;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException{
    private final String statusCode;

    protected HttpException(String statusCode, String message){
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(String message){
        super(message);
        this.statusCode = "HTTP_EXCEPTION";
    }
}
