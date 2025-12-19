package com.facture.exception;

import java.time.LocalDateTime;

public class ErrorResponse {

    public String message;
    public LocalDateTime timestamp;
    public int status;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
