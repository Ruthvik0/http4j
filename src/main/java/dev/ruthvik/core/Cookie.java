package dev.ruthvik.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Locale;

@ToString
@Builder(toBuilder = true)
@Getter
public class Cookie {

    private final List<String> sameSiteOptions = List.of("lax", "strict", "none");

    private final String name;
    private final String value;

    @Builder.Default
    private boolean httpOnly = false;

    @Builder.Default
    private long maxAge = -1;

    private String path;
    private String sameSite;
    private String domain;
    @Builder.Default
    private boolean secure = false;

    public Cookie(String name, String value, boolean httpOnly, long maxAge,
                  String path, String sameSite, String domain, boolean secure) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cookie name cannot be null or blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Cookie value cannot be null");
        }
        if (path != null && !path.startsWith("/")) {
            throw new IllegalArgumentException("Cookie path must start with '/' if set");
        }
        if (maxAge < -1) {
            throw new IllegalArgumentException("Cookie maxAge must be -1 or a positive number");
        }

        if (!sameSiteOptions.contains(sameSite.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Cookie same site must be either of these values" + sameSiteOptions);
        }

        this.name = name;
        this.value = value;
        this.httpOnly = httpOnly;
        this.maxAge = maxAge;
        this.path = path;
        this.sameSite = sameSite;
        this.domain = domain;
        this.secure = secure;
    }
}