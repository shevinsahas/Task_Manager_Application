package com.taskmanager.task.manager.ResponseHandler;


import com.taskmanager.task.manager.util.Messages;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.format.DateTimeParseException;
import java.util.List;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrorId());
        return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(ex.getStatus()));
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        String errorMessage = Messages.INVALID_FORMAT;

        if (mostSpecificCause instanceof DateTimeParseException) {
            errorMessage = Messages.INVALID_DATE_FORMAT;
        } else if (mostSpecificCause != null) {
            errorMessage = mostSpecificCause.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage,"INVALID_FORMAT" );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    //handling validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setErrorId(Messages.NOT_VALIDATED);

        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        String errorMessage = "";
        for (FieldError fieldError : fieldErrors) {
            errorMessage +=  fieldError.getDefaultMessage() + " ";
        }
        errorResponse.setMessage(errorMessage);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    
}


