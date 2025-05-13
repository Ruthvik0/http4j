package dev.ruthvik.core;

import dev.ruthvik.exception.RequestException;

import java.util.Optional;

@FunctionalInterface
public interface Middleware {
    Optional<Response> handle(Request request) throws RequestException;
}
