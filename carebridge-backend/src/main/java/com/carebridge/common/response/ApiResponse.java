package com.carebridge.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timeStamp
) {
    // successfully response with  data
    public static <T> ApiResponse<T> success(
        String message,
        T data
    ){
        return new ApiResponse<T>(
            true, 
            message, 
            data, 
            LocalDateTime.now()
        );

    }
    // successfully response without data
    public static ApiResponse<Void> success(
        String message

    ){
        return new ApiResponse<>(
            true, 
            message, 
            null,
            LocalDateTime.now()
        );
        

    }
    // failed response with error details
    public static <T> ApiResponse<T> failure(
        String message,
        T data
    ){
        return new ApiResponse<T>(false, message, data, LocalDateTime.now());

    }

    // Failed response without additional error details
    public static ApiResponse<Void> failure(
            String message
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                LocalDateTime.now()
        );
    }


}
