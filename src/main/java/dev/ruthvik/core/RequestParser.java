package dev.ruthvik.core;

import dev.ruthvik.exception.BadRequestException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class RequestParser {
    public static Request parse(InputStream input) {
        try {
            String requestLine = readLine(input);
            if (requestLine == null || requestLine.isEmpty()) {
                throw new BadRequestException("Empty request line");
            }

            String[] parts = requestLine.trim().split(" ");
            if (parts.length != 3) {
                throw new BadRequestException("Invalid request line");
            }

            String method = parts[0];
            String url = parts[1];
            String version = parts[2];

            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = readLine(input)).isEmpty()) {
                if (line.contains(":")) {
                    String[] headerParts = line.split(":", 2);
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }

            if ("HTTP/1.1".equals(version) && !headers.containsKey("Host")) {
                throw new BadRequestException("Missing Host header");
            }

            if (headers.containsKey("Transfer-Encoding") && headers.get("Transfer-Encoding").equals("chunked")) {
                throw new BadRequestException("Doesn't support chunked request");
            }

            String contentType = headers.getOrDefault("Content-Type", "");
            int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
            String body = "";
            Map<String, String> formFields = new HashMap<>();
            Map<String, MultiPartForm.FileItem> fileFields = new HashMap<>();

            if (contentType.startsWith("multipart/form-data")) {
                parseMultiPartForm(input, contentType, contentLength, formFields, fileFields);
            } else if (contentLength > 0) {
                body = new String(getContent(input, contentLength), StandardCharsets.UTF_8);
            }

            MultiPartForm multiPartForm = new MultiPartForm(formFields, fileFields);
            return new Request(method, url, headers, body, multiPartForm);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse request: " + e.getMessage());
        }
    }

    private static void parseMultiPartForm(InputStream input, String contentType, int contentLength, Map<String, String> formFields, Map<String, MultiPartForm.FileItem> fileFields) throws IOException {
        if (!contentType.contains("boundary=")) {
            throw new BadRequestException("Missing boundary in multipart form data");
        }
        String boundary = "--" + contentType.split("boundary=", 2)[1];
        byte[] content = getContent(input, contentLength);
        String rawBody = new String(content, StandardCharsets.ISO_8859_1);

        for (String part : rawBody.split(boundary)) {
            part = part.trim();
            if (part.isEmpty() || part.equals("--")) continue;

            int separatorIndex = part.indexOf("\r\n\r\n");
            if (separatorIndex == -1) continue; // invalid

            String headerSection = part.substring(0, separatorIndex);
            byte[] partBodyBytes = part.substring(separatorIndex + 4).getBytes(StandardCharsets.ISO_8859_1);

            Map<String, String> partHeaders = new HashMap<>();
            for (String headerLine : headerSection.split("\r\n")) {
                if (headerLine.trim().isEmpty()) continue;
                String[] headerParts = headerLine.split(":", 2);
                if (headerParts.length == 2) {
                    partHeaders.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }

            String disposition = partHeaders.get("Content-Disposition");
            if (disposition == null) continue;

            String name = null, filename = null;
            for (String param : disposition.split(";")) {
                param = param.trim();
                if (param.startsWith("name=")) {
                    name = param.split("=", 2)[1].replace("\"", "");
                }
                if (param.startsWith("filename=")) {
                    filename = param.split("=", 2)[1].replace("\"", "");
                }
            }

            if (filename == null) {
                formFields.put(name, new String(partBodyBytes, StandardCharsets.UTF_8).trim());
            } else {
                MultiPartForm.FileItem file = new MultiPartForm.FileItem();
                file.setFileName(filename);
                file.setContent(partBodyBytes);
                file.setContentType(partHeaders.getOrDefault("Content-Type", "application/octet-stream"));
                fileFields.put(name, file);
            }
        }
    }

    private static byte[] getContent(InputStream input, int contentLength) throws IOException {
        if (contentLength <= 0) return new byte[0];
        byte[] buffer = new byte[contentLength];
        int read = 0;
        while (read < contentLength) {
            int r = input.read(buffer, read, contentLength - read);
            if (r == -1) break;
            read += r;
        }
        return buffer;
    }

    private static String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
        int previous = -1, current;
        while ((current = input.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                break;
            }
            if (previous != -1) {
                lineBuffer.write(previous);
            }
            previous = current;
        }
        if (previous != -1 && current == -1) {
            lineBuffer.write(previous);
        }
        return lineBuffer.toString(StandardCharsets.ISO_8859_1);
    }
}