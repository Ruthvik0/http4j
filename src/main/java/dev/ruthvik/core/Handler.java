package dev.ruthvik.core;

import dev.ruthvik.exception.RequestException;

@FunctionalInterface
public interface Handler {
    Response handle(Request request) throws RequestException;
}