package com.carebridge.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.carebridge.common.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception){
        ApiResponse<Void> response= ApiResponse.failure(exception.getMessage());
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>>handleDuplicateResouce(
        DuplicateResourceException exception
    ){
        ApiResponse<Void> response =ApiResponse.failure(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>>
    handleValidationErrors(MethodArgumentNotValidException exception){
        Map<String,String> errors =new LinkedHashMap<>();
        exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(fieldError-> errors.putIfAbsent(fieldError.getField(),fieldError.getDefaultMessage()));
        ApiResponse<Map<String,String>> response = ApiResponse.failure("request validation failed",errors);
        return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);

    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception exception){
        log.error("unexpected exception occurred",exception);
        ApiResponse<Void> response=ApiResponse.failure("an unexpected error coourred");
        return ResponseEntity.
        status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
