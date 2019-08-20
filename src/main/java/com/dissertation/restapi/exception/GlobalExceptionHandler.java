package com.dissertation.restapi.exception;


import com.dissertation.restapi.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
@RequestMapping (produces = "application/json")
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadRequest(BadRequestException ex){
        return createErrorResponse(ex);
    }

    public ErrorResponse createErrorResponse(HttpException ex){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(ex.getStatusCode());
        errorResponse.setMessage(ex.getMessage());

        return errorResponse;
    }
}
