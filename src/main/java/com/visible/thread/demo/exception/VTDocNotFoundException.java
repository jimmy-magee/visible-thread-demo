package com.visible.thread.demo.exception;

import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class VTDocNotFoundException extends ResponseStatusException {

    private static final long serialVersionUID = 1L;

    public VTDocNotFoundException(String message) {
        super(NOT_FOUND, message);
    }

    public VTDocNotFoundException(String message, Throwable cause) {
        super(NOT_FOUND, message, cause);
    }

}