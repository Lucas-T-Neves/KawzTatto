package com.kawz.tattoo.support;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/** Servidor HTTP falso (JDK) que faz o papel do APITemplate/Nylas nos testes e registra o que recebeu. */
public final class FakeServer implements AutoCloseable {

    public record Req(String method, String uri, Map<String, String> headers, String body) {}

    public record Resp(int status, String body) {}

    public final List<Req> requests = new CopyOnWriteArrayList<>();
    private final HttpServer server;
    private volatile Function<Req, Resp> handler;

    public FakeServer(Function<Req, Resp> handler) throws IOException {
        this.handler = handler;
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] in = exchange.getRequestBody().readAllBytes();
            Map<String, String> headers = new HashMap<>();
            exchange.getRequestHeaders().forEach((k, v) -> headers.put(k.toLowerCase(), String.join(",", v)));
            Req req = new Req(exchange.getRequestMethod(), exchange.getRequestURI().toString(), headers,
                    new String(in, StandardCharsets.UTF_8));
            requests.add(req);
            Resp resp = this.handler.apply(req);
            byte[] out = resp.body().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            if (out.length == 0) {
                exchange.sendResponseHeaders(resp.status(), -1);
            } else {
                exchange.sendResponseHeaders(resp.status(), out.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(out); }
            }
            exchange.close();
        });
        server.start();
    }

    public void responder(Function<Req, Resp> novo) { this.handler = novo; }

    public String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }

    public void limpar() { requests.clear(); }

    public Req ultima() { return requests.get(requests.size() - 1); }

    @Override
    public void close() { server.stop(0); }
}
