package dev.ruthvik.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.ruthvik.enums.Header;
import dev.ruthvik.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ToString
public class Request {
    private static final Gson gson = new Gson();

    @Getter
    private final String method;
    @Getter
    private final String url;
    private final Map<String, String> headers;
    @Getter
    private final String body;

    private final MultiPartForm multiPartForm;

    @Setter(AccessLevel.PACKAGE)
    private Map<String, String> pathParams = new HashMap<>();

    @Setter(AccessLevel.PACKAGE)
    private Map<String, String> queryParams = new HashMap<>();

     Request(String method, String url, Map<String, String> headers, String body,
                   MultiPartForm multiPartForm) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
        this.multiPartForm = multiPartForm;
    }

    private void ensureContentType(String expectedType) {
        String actual = headers.getOrDefault("Content-Type", "").toLowerCase(Locale.ROOT);
        if (!actual.startsWith(expectedType.toLowerCase())) {
            throw new BadRequestException("Expected Content-Type: " + expectedType + " but got: " + actual);
        }
    }

    public String getText() {
        ensureContentType("text/plain");
        return body;
    }

    public <T> T getJson(Class<T> clazz) {
        ensureContentType("application/json");
        try {
            return gson.fromJson(body, clazz);
        } catch (JsonSyntaxException e) {
            throw new BadRequestException("Error in parsing request [invalid json]");
        }
    }

    public Map<String, String> getFormData() {
        ensureContentType("application/x-www-form-urlencoded");
        Map<String, String> formData = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8) : "";
            formData.put(key, value);
        }
        return formData;
    }

    public MultiPartForm getMultiPartFormData(){
         ensureContentType("multipart/form-data");
         return this.multiPartForm;
    }

    public boolean hasHeader(Header header) {
        return headers.containsKey(header.value());
    }

    public Optional<String> getHeader(Header header) {
        return Optional.ofNullable(headers.get(header.value()));
    }

    public String getHeaderOrThrow(Header header) {
        String value = headers.get(header.value());
        if (value == null) {
            throw new BadRequestException("Missing required header: " + header.value());
        }
        return value;
    }

    public Optional<String> getQueryParam(String key) {
        return Optional.ofNullable(queryParams.get(key));
    }

    public String getQueryParamOrThrow(String key) {
        String value = queryParams.get(key);
        if (value == null) {
            throw new BadRequestException("Missing required query parameter: " + key);
        }
        return value;
    }

    public Optional<String> getPathParam(String key) {
        return Optional.ofNullable(pathParams.get(key));
    }

    public String getPathParamOrThrow(String key) {
        String value = pathParams.get(key);
        if (value == null) {
            throw new BadRequestException("Missing required path parameter: " + key);
        }
        return value;
    }

    public List<Cookie> getAllCookies() {
        String cookieHeader = headers.getOrDefault("Cookie", "").trim();
        if (cookieHeader.isEmpty()) {
            return Collections.emptyList();
        }
        List<Cookie> cookies = new ArrayList<>();
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.split("=", 2);
            if (parts.length == 2) {
                cookies.add(Cookie.builder()
                        .name(parts[0].trim())
                        .value(parts[1].trim())
                        .build());
            }
        }
        return cookies;
    }

    public Optional<Cookie> getCookie(String name) {
        return getAllCookies().stream()
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }
}