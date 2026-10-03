package co.lettermint;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;

/** A local HTTP server that records requests and answers with queued or default responses. */
final class FakeServer implements AutoCloseable {
    /** A recorded request. Header names are lower-case. */
    record Request(String method, String path, String rawQuery, Map<String, List<String>> headers, String body) {
        String header(String name) {
            List<String> values = headers.get(name.toLowerCase());
            return values == null || values.isEmpty() ? null : values.get(0);
        }

        String target() {
            return path + (rawQuery == null ? "" : "?" + rawQuery);
        }
    }

    /** A response: status, headers, body, and an optional delay before the body. */
    record Response(int status, Map<String, String> headers, String body, long delayMillis, boolean stallBody) {
        static Response json(int status, String body) {
            return new Response(status, Map.of("Content-Type", "application/json"), body, 0, false);
        }

        static Response text(int status, String contentType, String body) {
            return new Response(status, contentType == null ? Map.of() : Map.of("Content-Type", contentType), body, 0, false);
        }

        static Response empty(int status) {
            return new Response(status, Map.of(), null, 0, false);
        }

        Response header(String name, String value) {
            Map<String, String> next = new LinkedHashMap<>(headers);
            next.put(name, value);
            return new Response(status, next, body, delayMillis, stallBody);
        }

        Response delay(long millis) {
            return new Response(status, headers, body, millis, stallBody);
        }

        /** Sends the headers at once and then waits {@code delay} before the body. */
        Response stallBody(long millis) {
            return new Response(status, headers, body, millis, true);
        }
    }

    private final HttpServer server;
    private final List<Request> requests = Collections.synchronizedList(new ArrayList<>());
    private final Deque<Response> queue = new ArrayDeque<>();
    private volatile Function<Request, Response> fallback = request -> Response.json(200, "{}");

    FakeServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", this::handle);
        server.start();
    }

    String origin() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    String baseUrl() {
        return origin() + "/v1";
    }

    /** Answers the next request with {@code response}; later requests use the fallback. */
    synchronized FakeServer enqueue(Response response) {
        queue.add(response);
        return this;
    }

    FakeServer fallback(Function<Request, Response> fallback) {
        this.fallback = fallback;
        return this;
    }

    List<Request> requests() {
        synchronized (requests) {
            return List.copyOf(requests);
        }
    }

    Request last() {
        List<Request> all = requests();
        return all.get(all.size() - 1);
    }

    private synchronized Response next(Request request) {
        Response queued = queue.poll();
        return queued != null ? queued : fallback.apply(request);
    }

    private void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), List.copyOf(values)));
        String body;
        try (InputStream in = exchange.getRequestBody()) {
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Request request = new Request(exchange.getRequestMethod(), uri.getRawPath(), uri.getRawQuery(), headers, body);
        requests.add(request);
        Response response = next(request);
        try {
            if (response.delayMillis() > 0 && !response.stallBody()) {
                Thread.sleep(response.delayMillis());
            }
            response.headers().forEach((name, value) -> exchange.getResponseHeaders().add(name, value));
            byte[] bytes = response.body() == null ? new byte[0] : response.body().getBytes(StandardCharsets.UTF_8);
            if (response.stallBody()) {
                exchange.sendResponseHeaders(response.status(), bytes.length + 1L);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                    out.flush();
                    Thread.sleep(response.delayMillis());
                    out.write(' ');
                }
                return;
            }
            boolean noBody = response.status() == 204 || response.status() == 304 || bytes.length == 0;
            exchange.sendResponseHeaders(response.status(), noBody ? -1 : bytes.length);
            if (!noBody) {
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            }
        } catch (InterruptedException | IOException error) {
            Thread.currentThread().interrupt();
        } finally {
            exchange.close();
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
