package runner;

import dev.ruthvik.core.HttpServer;
import dev.ruthvik.core.Response;
import dev.ruthvik.enums.HttpStatus;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        HttpServer server = new HttpServer(3000);

        // Middleware: Logger
        server.addGlobalMiddleware((req) -> {
            System.out.println(req.getMethod() + " " + req.getUrl());
        });

        // Route: Home
        server.post("/", (request) -> {
            request.getMultiPartFormData().getAllFiles().forEach((key, fileItem) -> {
                        System.out.println("-> getting " + key);
                        System.out.println(fileItem.getFileName() + "-- " + fileItem.getContentType());
                        String filePath = fileItem.getFileName();

                        try (FileOutputStream fos = new FileOutputStream(filePath)) {
                            // Write the byte array to the file
                            fos.write(fileItem.getContent());

                            System.out.println("Data has been written to the file successfully.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );

            Response res = new Response();
            res.setHtml("<h1>Hello World!</h1>", HttpStatus.OK);
            return res;
        });

        // Start server
        server.run();
    }
}