package com.teste.apis.integration.apoio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.apis.exception.IntegrationException;

/**
 * Tudo que se repete em todo client HTTP: marcar erro do provedor, ler o corpo
 * da resposta e traduzir para {@link IntegrationException}.
 *
 * <p>Antes esta logica estava copiada em cada metodo de cada client. Aqui fica
 * uma vez so; cada client mantem apenas o que e especifico dele (URL, corpo,
 * credencial).</p>
 */
public final class ProvedorHttp {

    private static final Logger log = LoggerFactory.getLogger(ProvedorHttp.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProvedorHttp() {
    }

    /**
     * Aplica os tratamentos de erro (4xx e 5xx) em uma resposta do RestClient.
     * Uso: {@code ProvedorHttp.comTratamentoDeErro(restClient.get()...retrieve()).body(...)}
     */
    public static RestClient.ResponseSpec comTratamentoDeErro(RestClient.ResponseSpec spec) {
        return spec
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw novoErro(res); })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> { throw novoErro(res); });
    }

    /** Timeout ou falha de rede na chamada (vira 504 na API). */
    public static IntegrationException timeout(String provedor, String mensagem) {
        return IntegrationException.timeout(provedor, mensagem);
    }

    /**
     * Traduz o erro do provedor para a excecao da aplicacao:
     * 401/403 -> credencial invalida, 429 -> limite de requisicoes,
     * resto -> falha de integracao (502).
     */
    public static IntegrationException traduzir(ErroProvedor erro, String provedor) {
        int status = erro.getStatus();

        if (status == 401 || status == 403) {
            log.error("Credencial invalida no provedor={} status={}", provedor, status);
            return IntegrationException.credencialInvalida(provedor);
        }
        if (status == 429) {
            log.warn("Limite de requisicoes excedido no provedor={}", provedor);
            return IntegrationException.muitosRequests(provedor,
                    "Limite de requisicoes do provedor excedido", erro.getRetryAfter());
        }

        String mensagem = extrairMensagem(erro.getCorpo());
        log.error("Falha no provedor={} status={} mensagem={}", provedor, status, mensagem);
        return IntegrationException.falhaProvedor(provedor, mensagem, status);
    }

    /** Mensagem amigavel a partir do corpo de erro do provedor (JSON ou texto). */
    public static String extrairMensagem(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            return "Erro retornado pelo provedor";
        }
        try {
            Map<String, Object> mapa = MAPPER.readValue(corpo, new TypeReference<Map<String, Object>>() { });
            Object message = mapa.get("message");
            Object error = mapa.get("error");
            if (message != null) {
                return String.valueOf(message);
            }
            if (error != null) {
                return String.valueOf(error);
            }
        } catch (Exception e) {
            // corpo nao e JSON: segue com a mensagem generica
        }
        return "Erro retornado pelo provedor";
    }

    /** Le o corpo da resposta de erro como texto. */
    public static String lerCorpo(ClientHttpResponse response) {
        try (InputStream body = response.getBody()) {
            return new String(body.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /** Le o cabecalho Retry-After, quando existir. */
    public static Long lerRetryAfter(ClientHttpResponse response) {
        try {
            String valor = response.getHeaders().getFirst("Retry-After");
            return valor == null ? null : Long.valueOf(valor.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static ErroProvedor novoErro(ClientHttpResponse response) throws IOException {
        return new ErroProvedor(response.getStatusCode().value(), lerCorpo(response), lerRetryAfter(response));
    }
}
