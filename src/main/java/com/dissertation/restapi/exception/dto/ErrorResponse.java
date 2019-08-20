package com.dissertation.restapi.exception.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String statusCode;
}
