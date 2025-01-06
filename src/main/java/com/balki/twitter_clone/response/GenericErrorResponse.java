package com.balki.twitter_clone.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GenericErrorResponse {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    public GenericErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
