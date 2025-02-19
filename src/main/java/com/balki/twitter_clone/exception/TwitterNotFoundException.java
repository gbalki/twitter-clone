package com.balki.twitter_clone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class TwitterNotFoundException extends RuntimeException {

    public TwitterNotFoundException(String message) {
        super(message);
    }
}
