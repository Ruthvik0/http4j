package dev.ruthvik.core;

import dev.ruthvik.enums.HttpMethod;
import dev.ruthvik.enums.HttpStatus;
import dev.ruthvik.exception.RequestException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HttpServer {
    private final int port;
    private final Router router;
    private final List<Middleware> pendingMiddlewares = new ArrayList<>();

    public HttpServer(int port) {
        this.port = port;
        this.router = new Router();
    }

    public final void addGlobalMiddleware(Middleware middleware) {
        router.addGlobalMiddleware(middleware);
    }

    public final void addGlobalMiddleware(SimpleMiddleware middleware) {
        router.addGlobalMiddleware(req -> {
            middleware.handle(req);
            return Optional.empty();
        });
    }

    private HttpServer addMiddlewares(Middleware... middlewares) {
        pendingMiddlewares.addAll(Arrays.asList(middlewares));
        return this;
    }

    public HttpServer addMiddleware(Middleware... middlewares) {
        return addMiddlewares(middlewares);
    }

    public HttpServer addMiddleware(SimpleMiddleware... middlewares) {
        Middleware[] converted = Arrays.stream(middlewares)
                .map(simpleMiddleware -> (Middleware) req -> {
                    simpleMiddleware.handle(req);
                    return Optional.empty();
                })
                .toArray(Middleware[]::new);
        return addMiddlewares(converted);
    }

    public final void add(HttpMethod method, String url, Handler handler) {
        router.add(method, url, new ArrayList<>(pendingMiddlewares), handler);
        pendingMiddlewares.clear();
    }

    public final void get(String url, Handler handler) {
        router.add(HttpMethod.GET, url, new ArrayList<>(pendingMiddlewares), handler);
        pendingMiddlewares.clear();
    }

    public final void post(String url, Handler handler) {
        router.add(HttpMethod.POST, url, new ArrayList<>(pendingMiddlewares), handler);
        pendingMiddlewares.clear();
    }

    public final void put(String url, Handler handler) {
        router.add(HttpMethod.PUT, url, new ArrayList<>(pendingMiddlewares), handler);
        pendingMiddlewares.clear();
    }

    public final void delete(String url, Handler handler) {
        router.add(HttpMethod.DELETE, url, new ArrayList<>(pendingMiddlewares), handler);
        pendingMiddlewares.clear();
    }

    public void run() {
        System.out.printf("Running at http://localhost:%d%n", port);

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Thread.ofVirtual().start(()->{
                    try (Socket client = server.accept()) {
                        InputStream input = client.getInputStream();
                        OutputStream writer = client.getOutputStream();
                        try {
                            Request request = RequestParser.parse(input);
                            Response response = router.match(request);
                            response.sendOutput(writer);
                        } catch (RequestException e) {
                            Response response = new Response();
                            response.setText(e.getMessage(), e.getStatus());
                            response.sendOutput(writer);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            Response response = new Response();
                            response.setText("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
                            response.sendOutput(writer);
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                }).join();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}