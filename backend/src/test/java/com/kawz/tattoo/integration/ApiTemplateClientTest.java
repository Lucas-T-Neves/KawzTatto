package com.kawz.tattoo.integration;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import com.kawz.tattoo.support.FakeServer;
import com.kawz.tattoo.support.FakeServer.Resp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testa o ApiTemplateClient contra um servidor HTTP falso (sem rede externa). */
class ApiTemplateClientTest {
    private FakeServer server;

    @BeforeEach
    void subir() throws Exception {
        server = new FakeServer(req -> new Resp(200,
                "{\"status\":\"success\",\"download_url\":\"https://pdf.exemplo/c.pdf\",\"transaction_ref\":\"tx-1\"}"));
    }

    @AfterEach
    void descer() { server.close(); }

    @Test
    void geraPdfEEnviaChaveTemplateEDados() {
        var client = new ApiTemplateClient(server.url(), "chave-teste", "tpl-1");

        var pdf = client.gerarContrato(Map.of("nome_cliente", "Ana"));

        assertThat(pdf.downloadUrl()).isEqualTo("https://pdf.exemplo/c.pdf");
        assertThat(pdf.transactionRef()).isEqualTo("tx-1");
        var req = server.ultima();
        assertThat(req.method()).isEqualTo("POST");
        assertThat(req.uri()).startsWith("/v2/create-pdf").contains("template_id=tpl-1");
        assertThat(req.headers().get("x-api-key")).isEqualTo("chave-teste");
        assertThat(req.body()).contains("\"nome_cliente\"").contains("Ana");
    }

    @Test
    void erroHttpDoApiTemplateViraIntegracaoException() {
        server.responder(req -> new Resp(500, "{\"status\":\"error\"}"));
        var client = new ApiTemplateClient(server.url(), "chave-teste", "tpl-1");

        assertThatThrownBy(() -> client.gerarContrato(Map.of("a", "b")))
                .isInstanceOf(IntegracaoException.class);
    }

    @Test
    void respostaSemDownloadUrlViraIntegracaoException() {
        server.responder(req -> new Resp(200, "{\"status\":\"success\"}"));
        var client = new ApiTemplateClient(server.url(), "chave-teste", "tpl-1");

        assertThatThrownBy(() -> client.gerarContrato(Map.of("a", "b")))
                .isInstanceOf(IntegracaoException.class);
    }

    @Test
    void semChaveOuTemplateNaoChamaAApi() {
        var semChave = new ApiTemplateClient(server.url(), "", "tpl-1");
        var semTemplate = new ApiTemplateClient(server.url(), "chave", "");

        assertThatThrownBy(() -> semChave.gerarContrato(Map.of())).isInstanceOf(IntegracaoException.class);
        assertThatThrownBy(() -> semTemplate.gerarContrato(Map.of())).isInstanceOf(IntegracaoException.class);
        assertThat(server.requests).isEmpty();
    }
}
