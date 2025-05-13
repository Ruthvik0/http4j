package dev.ruthvik.exception;

import dev.ruthvik.enums.HttpStatus;
import lombok.Getter;

public class RequestException extends RuntimeException {
    @Getter
    private final HttpStatus status;
    public RequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
