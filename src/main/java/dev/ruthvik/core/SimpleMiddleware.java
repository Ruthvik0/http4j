package dev.ruthvik.core;

import dev.ruthvik.exception.RequestException;

@FunctionalInterface
public interface SimpleMiddleware {
    void handle(Request request) throws RequestException;
}
