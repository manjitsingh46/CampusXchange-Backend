package com.campusxchange.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Custom API exception for consistent error handling
 */
@Data
@AllArgsConstructor
@Builder
public class ApiException extends RuntimeException {

    private String message;
    private int status;
    private String errorCode;
    private Object data;

    public ApiException(String message, int status) {
        super(message);
        this.message = message;
        this.status = status;
        this.errorCode = "API_ERROR";
    }

    public ApiException(String message, int status, String errorCode) {
        super(message);
        this.message = message;
        this.status = status;
        this.errorCode = errorCode;
    }
}
