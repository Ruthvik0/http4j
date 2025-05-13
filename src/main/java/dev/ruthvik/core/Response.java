package dev.ruthvik.core;

import com.google.gson.Gson;
import dev.ruthvik.enums.Header;
import dev.ruthvik.enums.HttpStatus;
import lombok.ToString;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ToString
public class Response {
    private static final Gson gson = new Gson();
    private static final String HTTP_VERSION = "HTTP/1.1";

    private HttpStatus status;
    private final Map<String, String> headers = new HashMap<>();
    private final List<Cookie> cookies = new ArrayList<>();
    private byte[] bodyBytes = new byte[0];

    public void setHeader(Header key, String value) {
        headers.put(key.value(), value);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public void setText(String text, HttpStatus status) {
        this.bodyBytes = text.getBytes(StandardCharsets.UTF_8);
        this.status = status;
        this.headers.put("Content-Type", "text/plain; charset=utf-8");
    }

    public void setHtml(String html, HttpStatus status) {
        this.bodyBytes = html.getBytes(StandardCharsets.UTF_8);
        this.status = status;
        this.headers.put("Content-Type", "text/html; charset=utf-8");
    }

    public void setJson(Object o, HttpStatus status) {
        this.bodyBytes = gson.toJson(o).getBytes(StandardCharsets.UTF_8);
        this.status = status;
        this.headers.put("Content-Type", "application/json; charset=utf-8");
    }

    public void send(byte[] body, HttpStatus status) {
        this.bodyBytes = body;
        this.status = status;
    }

    public void redirect(String url) {
        String redirectUrl = url;
        if (!redirectUrl.startsWith("/")) {
            redirectUrl = "/".concat(url);
        }
        this.setHeader(Header.LOCATION, redirectUrl);
        this.send(new byte[0], HttpStatus.FOUND);
    }

    private List<String> cookieHeaders() {
        if (cookies.isEmpty()) return Collections.emptyList();

        List<String> headers = new ArrayList<>();
        for (Cookie cookie : this.cookies) {
            StringBuilder header = new StringBuilder();
            header.append("Set-Cookie: ");
            header.append(cookie.getName().trim()).append('=').append(cookie.getValue().trim()).append("; ");

            if (cookie.isHttpOnly()) header.append("HttpOnly; ");
            if (cookie.getMaxAge() > 0) header.append("Max-Age=").append(cookie.getMaxAge()).append("; ");
            if (cookie.getPath() != null && !cookie.getPath().isEmpty()) {
                String path = cookie.getPath().startsWith("/") ? cookie.getPath() : "/" + cookie.getPath();
                header.append("Path=").append(path).append("; ");
            }
            if (cookie.getDomain() != null && !cookie.getDomain().isEmpty()) {
                header.append("Domain=").append(cookie.getDomain()).append("; ");
            }
            if (cookie.getSameSite() != null) {
                header.append("SameSite=").append(cookie.getSameSite()).append("; ");
            }
            if (cookie.isSecure()) header.append("Secure; ");

            headers.add(header.toString().trim());
        }
        return headers;
    }


    private String buildResponseHttpHeaders() {
        if (status == null) {
            throw new IllegalStateException("Response status must be set before sending the response.");
        }

        StringBuilder builder = new StringBuilder();

        builder.append(HTTP_VERSION)
                .append(" ")
                .append(status.code())
                .append(" ")
                .append(status.reason())
                .append("\r\n");

        headers.putIfAbsent("Content-Length", String.valueOf(bodyBytes.length));

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }

        for (String cookieHeader : cookieHeaders()) {
            builder.append(cookieHeader).append("\r\n");
        }

        builder.append("\r\n");
        return builder.toString();
    }

    void sendOutput(OutputStream clientStream) throws IOException {
        byte[] headersBytes = buildResponseHttpHeaders().getBytes(StandardCharsets.ISO_8859_1);
        clientStream.write(headersBytes);
        clientStream.write(bodyBytes);
        clientStream.flush();
    }
}