package com.balki.twitter_clone.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FileAttachmentNotFoundException extends RuntimeException {

    public FileAttachmentNotFoundException(String message) {
        super(message);
    }
}
