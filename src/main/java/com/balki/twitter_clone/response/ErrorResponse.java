package com.balki.twitter_clone.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private Map<String,String> message;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, Map message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
