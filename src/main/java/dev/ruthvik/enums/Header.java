package dev.ruthvik.enums;

public enum Header {
    // -- General headers
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    DATE("Date"),
    PRAGMA("Pragma"),
    TRANSFER_ENCODING("Transfer-Encoding"),
    UPGRADE("Upgrade"),
    VIA("Via"),
    WARNING("Warning"),

    // -- Request headers
    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    AUTHORIZATION("Authorization"),
    COOKIE("Cookie"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    ORIGIN("Origin"),
    REFERER("Referer"),
    USER_AGENT("User-Agent"),

    // -- Response headers
    CONTENT_LENGTH("Content-Length"),
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    SERVER("Server"),
    SET_COOKIE("Set-Cookie"),
    WWW_AUTHENTICATE("WWW-Authenticate"),

    // -- CORS headers
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
    ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
    ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
    ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),

    // -- Security headers
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
    X_FRAME_OPTIONS("X-Frame-Options"),
    X_XSS_PROTECTION("X-XSS-Protection");

    private final String value;

    Header(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
