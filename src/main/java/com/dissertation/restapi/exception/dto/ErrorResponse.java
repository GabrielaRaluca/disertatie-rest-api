package com.dissertation.restapi.exception.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private Boolean success = false;
    private String message;
    private String statusCode;
}
