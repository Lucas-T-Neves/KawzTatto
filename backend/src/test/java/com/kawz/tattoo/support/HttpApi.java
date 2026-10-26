package com.kawz.tattoo.support;

import com.jayway.jsonpath.JsonPath;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Cliente HTTP mínimo (JDK) para chamar a API real subida em porta aleatória. */
public final class HttpApi {

    public record R(int status, String body) {
        public <T> T json(String path) { return JsonPath.read(body, path); }
    }

    public record Auth(String token, int tatuadorId) {}

    private final HttpClient http = HttpClient.newHttpClient();
    private final String base;

    public HttpApi(int port) { this.base = "http://127.0.0.1:" + port; }

    public R send(String method, String path, String json, String token) {
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path));
            if (token != null) b.header("Authorization", "Bearer " + token);
            if (json != null) b.header("Content-Type", "application/json");
            b.method(method, json == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new R(resp.statusCode(), resp.body());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public R get(String path, String token) { return send("GET", path, null, token); }
    public R post(String path, String json, String token) { return send("POST", path, json, token); }
    public R put(String path, String json, String token) { return send("PUT", path, json, token); }
    public R patch(String path, String json, String token) { return send("PATCH", path, json, token); }
    public R delete(String path, String token) { return send("DELETE", path, null, token); }

    /** Upload multipart com o campo "arquivo". */
    public R upload(String path, String token, String nomeArquivo, String contentType, byte[] conteudo) {
        try {
            String boundary = "----kawz" + UUID.randomUUID();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\""
                    + nomeArquivo + "\"\r\nContent-Type: " + contentType + "\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.write(conteudo);
            out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()));
            if (token != null) b.header("Authorization", "Bearer " + token);
            HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new R(resp.statusCode(), resp.body());
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    // ---------- fixtures ----------

    public Auth autenticar() {
        String email = "t" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        R criado = post("/api/v1/tatuadores",
                "{\"nome\":\"Tatuador Teste\",\"email\":\"" + email + "\",\"senha\":\"senha-segura\"}", null);
        if (criado.status() != 201) throw new IllegalStateException("registro falhou: " + criado);
        int id = criado.json("$.id");
        R login = post("/api/v1/tatuadores/autenticacao",
                "{\"email\":\"" + email + "\",\"senha\":\"senha-segura\"}", null);
        String token = login.json("$.token");
        return new Auth(token, id);
    }

    public int criarCliente(String token, String nome) {
        R r = post("/api/v1/clientes", "{\"nome\":\"" + nome + "\",\"telefone\":\"11999999999\",\"cpf\":\"123.456.789-00\","
                + "\"linkConversa\":\"https://wa.me/5511999999999\",\"status\":\"LEAD\"}", token);
        if (r.status() != 201) throw new IllegalStateException("cliente falhou: " + r);
        return r.json("$.id");
    }

    public int criarProjeto(String token, int tatuadorId, int clienteId) {
        R r = post("/api/v1/projetos", "{\"tatuadorId\":" + tatuadorId + ",\"clienteId\":" + clienteId
                + ",\"descricao\":\"Fechar braco\",\"statusProjeto\":\"ORCAMENTO\",\"valorTotal\":3000.00,"
                + "\"valorSinal\":500.00,\"quantidadeSessoes\":3}", token);
        if (r.status() != 201) throw new IllegalStateException("projeto falhou: " + r);
        return r.json("$.id");
    }
}
