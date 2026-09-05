package com.carebridge.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure("invalid email or password");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ApiResponse<Void>> handleInactiveAccount(
            RuntimeException exception
    ) {
        ApiResponse<Void> response =
                ApiResponse.failure("account is not active");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            InvalidTokenException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure("you do not have permission to access this resource"));
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

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(RuntimeException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception exception){
        log.error("unexpected exception occurred",exception);
        ApiResponse<Void> response=ApiResponse.failure("an unexpected error coourred");
        return ResponseEntity.
        status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
