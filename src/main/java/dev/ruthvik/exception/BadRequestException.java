package dev.ruthvik.exception;

import dev.ruthvik.enums.HttpStatus;

public class BadRequestException extends RequestException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
