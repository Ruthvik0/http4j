package dev.ruthvik.core;

import dev.ruthvik.enums.HttpMethod;
import dev.ruthvik.enums.HttpStatus;
import dev.ruthvik.exception.RequestException;

import java.util.*;

class Router {
    private final Map<String, Route> routes = new HashMap<>();
    private final List<Middleware> globalMiddlewares = new ArrayList<>();

    public final void add(HttpMethod method, String url, List<Middleware> middlewareList, Handler handler) {
        routes.put(url, new Route(method.name(), middlewareList, handler));
    }

    void addGlobalMiddleware(Middleware middleware) {
        globalMiddlewares.add(middleware);
    }

    Response match(Request req) {
        String method = req.getMethod();
        String url = req.getUrl();

        for (Map.Entry<String, Route> entry : routes.entrySet()) {
            String routePattern = entry.getKey();
            Route route = entry.getValue();

            if (matchRoutePattern(routePattern, url, req)) {
                if (!route.method().equalsIgnoreCase(method)) {
                    throw new RequestException("Method [%s] not allowed to this route".formatted(method), HttpStatus.METHOD_NOT_ALLOWED);
                }

                for (Middleware middleware : globalMiddlewares) {
                    Optional<Response> globalMiddlewareResponse = middleware.handle(req);
                    if (globalMiddlewareResponse.isPresent()) {
                        return globalMiddlewareResponse.get();
                    }
                }

                for (Middleware middleware : route.middlewares()) {
                    Optional<Response> middlewareResponse = middleware.handle(req);
                    if (middlewareResponse.isPresent()) {
                        return middlewareResponse.get();
                    }
                }
                return route.handler().handle(req);
            }
        }
        throw new RequestException("Route not found", HttpStatus.NOT_FOUND);
    }

    private boolean matchRoutePattern(String routePattern, String url, Request req) {
        // Clean up the URL (ignore query params)
        String cleanUrl = url.split("\\?")[0];
        Map<String, String> pathParams = new HashMap<>();

        String[] routeParts = routePattern.split("/");
        String[] urlParts = cleanUrl.split("/");

        if (routeParts.length != urlParts.length) {
            return false;
        }

        for (int i = 0; i < routeParts.length; i++) {
            String routePart = routeParts[i];
            String urlPart = urlParts[i];

            // Skip empty parts (e.g., leading or trailing slashes)
            if (routePart.isEmpty()) continue;

            // If the route part is a parameter (e.g., :id), store it in the pathParams map
            if (routePart.startsWith(":")) {
                String paramName = routePart.substring(1); // remove the ':'
                pathParams.put(paramName, urlPart);
            }
            // If the route part is a wildcard (e.g., *), match anything in this segment
            else if (routePart.equals("*")) {
                // Wildcard match: We don't need to store anything, just skip it
                continue;
            }
            // If it's a static part, and it doesn't match the URL part, return false
            else if (!routePart.equals(urlPart)) {
                return false;
            }
        }

        req.setPathParams(pathParams);
        parseQueryParams(url, req);
        return true;
    }


    private void parseQueryParams(String url, Request req) {
        if (!url.contains("?")) return;

        String queries = url.split("\\?", 2)[1];
        String[] queryPairs = queries.split("&");
        Map<String, String> queryParams = new HashMap<>();
        for (String pair : queryPairs) {
            String[] values = pair.split("=");
            if (values.length == 2) {
                queryParams.put(values[0].trim(), values[1].trim());
            }
        }
        req.setQueryParams(queryParams);
    }

    record Route(
            String method,
            List<Middleware> middlewares,
            Handler handler
    ) {
    }
}