package com.kawz.tattoo.api;

import com.kawz.tattoo.support.HttpApi;
import com.kawz.tattoo.support.HttpApi.Auth;
import com.kawz.tattoo.support.HttpApi.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sem nenhuma chave configurada: o app sobe normalmente, o Nylas apenas não cria evento
 * e Cloudinary/APITemplate respondem 502 com mensagem clara (nunca 500).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:kawz-desligadas;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "apitemplate.api-key=", "apitemplate.template-contrato-id=",
        "cloudinary.cloud-name=", "cloudinary.api-key=", "cloudinary.api-secret=",
        "nylas.api-key=", "nylas.grant-id="})
class IntegracoesDesligadasApiTest {

    @Value("${local.server.port}") int port;
    HttpApi api;
    Auth auth;
    String t;

    @BeforeEach
    void preparar() {
        api = new HttpApi(port);
        auth = api.autenticar();
        t = auth.token();
    }

    @Test
    void sessaoEhSalvaMesmoSemNylas() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Julia"));

        R r = api.post("/api/v1/sessoes", "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"2030-05-01T10:00:00\","
                + "\"localAtendimento\":\"Estudio\",\"statusSessao\":\"AGENDADA\"}", t);

        assertThat(r.status()).isEqualTo(201);
        int id = r.json("$.id");
        assertThat(api.patch("/api/v1/sessoes/" + id + "/remarcar", "{\"dataHorario\":\"2030-05-02T10:00:00\"}", t).status())
                .isEqualTo(200);
        assertThat(api.delete("/api/v1/sessoes/" + id, t).status()).isEqualTo(204);
    }

    @Test
    void pdfSemApiTemplateConfiguradoRetorna502() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Kaio"));
        R contrato = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        int contratoId = contrato.json("$.id");

        R r = api.post("/api/v1/contratos/" + contratoId + "/pdf", null, t);

        assertThat(r.status()).isEqualTo(502);
        assertThat(r.body()).contains("APITemplate");
    }

    @Test
    void uploadSemCloudinaryConfiguradoRetorna502() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Lia"));

        R r = api.upload("/api/v1/projetos/" + projetoId + "/imagens", t, "x.png", "image/png", new byte[]{1, 2, 3});

        assertThat(r.status()).isEqualTo(502);
        assertThat(r.body()).contains("Cloudinary");
    }
}
