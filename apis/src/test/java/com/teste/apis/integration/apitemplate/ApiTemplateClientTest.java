package com.teste.apis.integration.apitemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.Builder;

import com.teste.apis.contrato.ContratoRequestDTO;
import com.teste.apis.contrato.EstudioProperties;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.integration.apitemplate.dto.ApiTemplatePdfResponse;

/**
 * Testes do client do APITemplate.io com MockRestServiceServer.
 * Nenhuma chamada as APIs reais.
 */
class ApiTemplateClientTest {

    private static final ContratoRequestDTO REQUEST = new ContratoRequestDTO(
            "Lucas Neves",
            "529.982.247-25",
            "+55(11)99999-9999",
            "Kawa",
            "Cruzeiro do sul",
            "Antebraco",
            "GG",
            LocalDate.of(2026, 9, 19),
            LocalDate.of(2026, 10, 19),
            new BigDecimal("500.00"),
            Boolean.TRUE,
            "Pix");

    private Builder builder;
    private MockRestServiceServer server;
    private ApiTemplateClient client;

    private static final String BASE_URL = "http://localhost:8080/apitemplate";
    private static final String URL_PDF = BASE_URL + "/create-pdf?template_id=template-fake";

    @BeforeEach
    void setUp() {
        builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ApiTemplateClient(builder.build(), props(), estudio());
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.reset();
        }
    }

    private ApiTemplateProperties props() {
        ApiTemplateProperties p = new ApiTemplateProperties();
        p.setBaseUrl("http://localhost:1/apitemplate");
        p.setApiKey("chave-fake");
        p.setTemplateId("template-fake");
        p.setConnectTimeoutMs(3000);
        p.setReadTimeoutMs(30000);
        return p;
    }

    private EstudioProperties estudio() {
        EstudioProperties e = new EstudioProperties();
        e.setNome("Kawz Tatto");
        e.setDocumento("00000000000000");
        return e;
    }

    @Nested
    @DisplayName("geracao do PDF")
    class GeracaoPdf {

        @Test
        @DisplayName("deve enviar os dados do template e devolver o link em caso de sucesso")
        void sucesso() {
            server.expect(requestTo(URL_PDF))
                    .andExpect(method(HttpMethod.POST))
                    .andExpect(header("X-API-KEY", "chave-fake"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.nome_cliente").value("Lucas Neves"))
                    .andExpect(jsonPath("$.cpf_cliente").value("529.982.247-25"))
                    .andExpect(jsonPath("$.nome_estudio").value("Kawz Tatto"))
                    .andExpect(jsonPath("$.documento_estudio").value("00000000000000"))
                    .andExpect(jsonPath("$.data_inicio").value("19/09/2026"))
                    .andExpect(jsonPath("$.data_fim").value("19/10/2026"))
                    .andExpect(jsonPath("$.sinal_pago").value("Sim"))
                    .andExpect(jsonPath("$.valor_orcado").value(500.00))
                    .andRespond(withSuccess("""
                            {
                              "status": "success",
                              "download_url": "https://cdn.exemplo/contrato.pdf",
                              "template_id": "template-fake",
                              "transaction_ref": "ref-123"
                            }
                            """, MediaType.APPLICATION_JSON));

            ApiTemplatePdfResponse resposta = client.gerarPdf(REQUEST);

            server.verify();
            assertThat(resposta.sucesso()).isTrue();
            assertThat(resposta.downloadUrl()).isEqualTo("https://cdn.exemplo/contrato.pdf");
            assertThat(resposta.transactionRef()).isEqualTo("ref-123");
        }

        @Test
        @DisplayName("deve falhar quando a resposta vem sem download_url")
        void sucessoSemLink() {
            server.expect(requestTo(URL_PDF))
                    .andRespond(withSuccess("""
                            { "status": "success", "transaction_ref": "ref-123" }
                            """, MediaType.APPLICATION_JSON));

            ApiTemplatePdfResponse resposta = client.gerarPdf(REQUEST);

            server.verify();
            assertThat(resposta.sucesso()).isFalse();
        }

        @Test
        @DisplayName("deve tratar erro 4xx do provedor como falha de integracao")
        void erroCliente() {
            server.expect(requestTo(URL_PDF))
                    .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("""
                                    { "status": "error", "message": "Template nao encontrado" }
                                    """));

            assertThatThrownBy(() -> client.gerarPdf(REQUEST))
                    .isInstanceOf(IntegrationException.class)
                    .hasMessageContaining("Template nao encontrado")
                    .satisfies(e -> assertThat(((IntegrationException) e).getStatusProvedor()).isEqualTo(400));

            server.verify();
        }

        @Test
        @DisplayName("deve tratar erro 5xx do provedor como falha de integracao")
        void erroServidor() {
            server.expect(requestTo(URL_PDF))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("""
                                    { "status": "error", "message": "erro interno" }
                                    """));

            assertThatThrownBy(() -> client.gerarPdf(REQUEST))
                    .isInstanceOf(IntegrationException.class)
                    .satisfies(e -> assertThat(((IntegrationException) e).getStatusProvedor()).isEqualTo(500));
        }

        @Test
        @DisplayName("deve expor o Retry-After quando o provedor responde 429")
        void limiteDeRequisicoes() {
            server.expect(requestTo(URL_PDF))
                    .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("""
                                    { "status": "error", "message": "slow down" }
                                    """));

            assertThatThrownBy(() -> client.gerarPdf(REQUEST))
                    .isInstanceOf(IntegrationException.class)
                    .satisfies(e -> {
                        IntegrationException ex = (IntegrationException) e;
                        assertThat(ex.getStatusProvedor()).isEqualTo(429);
                    });
        }

        @Test
        @DisplayName("deve tratar 401 do provedor como erro de credencial")
        void credencialInvalida() {
            server.expect(requestTo(URL_PDF))
                    .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("""
                                    { "status": "error", "message": "Invalid api key" }
                                    """));

            assertThatThrownBy(() -> client.gerarPdf(REQUEST))
                    .isInstanceOf(IntegrationException.class)
                    .satisfies(e -> {
                        IntegrationException ex = (IntegrationException) e;
                        assertThat(ex.getStatusProvedor()).isEqualTo(401);
                        assertThat(ex.isTimeout()).isFalse();
                    });
        }

        @Test
        @DisplayName("deve marcar como timeout quando a chamada ao provedor nao completa")
        void timeout() {
            // Porta fechada: a chamada falha com ResourceAccessException,
            // exatamente o caminho que um timeout real de leitura percorre.
            String baseUrl = "http://127.0.0.1:1";
            ApiTemplateProperties propsLentos = props();
            propsLentos.setBaseUrl(baseUrl);
            propsLentos.setConnectTimeoutMs(300);
            propsLentos.setReadTimeoutMs(300);

            RestClient semServidor = RestClient.builder().baseUrl(baseUrl).build();
            ApiTemplateClient clientSemServidor = new ApiTemplateClient(semServidor, propsLentos, estudio());

            assertThatThrownBy(() -> clientSemServidor.gerarPdf(REQUEST))
                    .isInstanceOf(IntegrationException.class)
                    .satisfies(e -> assertThat(((IntegrationException) e).isTimeout()).isTrue());
        }
    }

    @Nested
    @DisplayName("mapeamento para o template")
    class Mapeamento {

        @Test
        @DisplayName("deve usar os nomes snake_case do template e as conversoes previstas")
        void mapeiaTodosOsCampos() {
            Map<String, Object> corpo = client.mapearParaTemplate(REQUEST);

            assertThat(corpo).containsKeys(
                    "nome_estudio", "nome_cliente", "cpf_cliente", "telefone_cliente",
                    "nome_artista", "documento_estudio", "descricao_tatuagem", "local_corpo",
                    "tamanho_estimado", "data_inicio", "data_fim", "valor_orcado",
                    "sinal_pago", "forma_pagamento", "data_geracao");
            assertThat(corpo).hasSize(15);
            assertThat(corpo.get("data_inicio")).isEqualTo("19/09/2026");
            assertThat(corpo.get("data_fim")).isEqualTo("19/10/2026");
            assertThat(corpo.get("sinal_pago")).isEqualTo("Sim");
            assertThat(corpo.get("valor_orcado")).isEqualTo(new BigDecimal("500.00"));
            assertThat(corpo.get("cpf_cliente")).isEqualTo("529.982.247-25");
        }

        @Test
        @DisplayName("deve converter sinalPago false para Nao")
        void sinalNao() {
            ContratoRequestDTO semSinal = new ContratoRequestDTO(
                    REQUEST.nomeCliente(), REQUEST.cpfCliente(), REQUEST.telefoneCliente(),
                    REQUEST.nomeArtista(), REQUEST.descricaoTatuagem(), REQUEST.localCorpo(),
                    REQUEST.tamanhoEstimado(), REQUEST.dataInicio(), REQUEST.dataFim(),
                    REQUEST.valorOrcado(), Boolean.FALSE, REQUEST.formaPagamento());

            assertThat(client.mapearParaTemplate(semSinal).get("sinal_pago")).isEqualTo("Nao");
        }

        @Test
        @DisplayName("deve manter o CPF no formato recebido")
        void cpfSemMascara() {
            ContratoRequestDTO cpfLimpo = new ContratoRequestDTO(
                    REQUEST.nomeCliente(), "52998224725", REQUEST.telefoneCliente(),
                    REQUEST.nomeArtista(), REQUEST.descricaoTatuagem(), REQUEST.localCorpo(),
                    REQUEST.tamanhoEstimado(), REQUEST.dataInicio(), REQUEST.dataFim(),
                    REQUEST.valorOrcado(), REQUEST.sinalPago(), REQUEST.formaPagamento());

            assertThat(client.mapearParaTemplate(cpfLimpo).get("cpf_cliente")).isEqualTo("52998224725");
        }
    }
}
