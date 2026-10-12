package com.teste.apis.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.teste.apis.exception.GlobalExceptionHandler;
import com.teste.apis.exception.IntegrationException;

/**
 * Testes da camada web do controller de contratos: status, validacao e
 * formato das respostas.
 */
@WebMvcTest(ContratoController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContratoService service;

    private static final String CONTRATO_VALIDO = """
            {
              "nomeCliente": "Lucas Neves",
              "cpfCliente": "529.982.247-25",
              "telefoneCliente": "+55(11)99999-9999",
              "nomeArtista": "Kawa",
              "descricaoTatuagem": "Cruzeiro do sul",
              "localCorpo": "Antebraco",
              "tamanhoEstimado": "GG",
              "dataInicio": "2026-09-19",
              "dataFim": "2026-10-19",
              "valorOrcado": 500.00,
              "sinalPago": true,
              "formaPagamento": "Pix"
            }
            """;

    @Test
    @DisplayName("POST /contratos com dados validos deve devolver 201 com Location e pdfUrl")
    void criaComSucesso() throws Exception {
        when(service.criar(any()))
                .thenReturn(new ContratoResponseDTO(1L, "Lucas Neves", StatusContrato.GERADO,
                        "https://cdn.exemplo/contrato.pdf", OffsetDateTime.now()));

        mockMvc.perform(post("/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONTRATO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/contratos/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("GERADO"))
                .andExpect(jsonPath("$.pdfUrl").value("https://cdn.exemplo/contrato.pdf"))
                .andExpect(jsonPath("$.nomeCliente").value("Lucas Neves"));
    }

    @Test
    @DisplayName("POST /contratos com dados invalidos deve devolver 400 e nao chamar o provedor")
    void rejeitaDadosInvalidos() throws Exception {
        String corpoInvalido = CONTRATO_VALIDO
                .replace("\"Lucas Neves\"", "\"Lu\"")
                .replace("\"529.982.247-25\"", "\"111.111.111-11\"")
                .replace("500.00", "-10.00");

        mockMvc.perform(post("/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.campo == 'nomeCliente')]").exists())
                .andExpect(jsonPath("$.errors[?(@.campo == 'cpfCliente')]").exists())
                .andExpect(jsonPath("$.errors[?(@.campo == 'valorOrcado')]").exists());

        org.mockito.Mockito.verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /contratos com dataFim antes de dataInicio deve devolver 400")
    void rejeitaPeriodoInvalido() throws Exception {
        String corpoInvalido = CONTRATO_VALIDO.replace("\"2026-10-19\"", "\"2026-01-01\"");

        mockMvc.perform(post("/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.campo == 'periodoValido')]").exists());
    }

    @Test
    @DisplayName("deve devolver 502 quando o provedor falha, sem expor o erro cru do provedor")
    void falhaDoProvedor() throws Exception {
        when(service.criar(any()))
                .thenThrow(IntegrationException.falhaProvedor("APITemplate.io", "Template nao encontrado", 400));

        MvcResult resultado = mockMvc.perform(post("/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONTRATO_VALIDO))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.provedor").value("APITemplate.io"))
                .andExpect(jsonPath("$.detail").value("Falha ao consultar o servico externo"))
                .andReturn();

        String corpo = resultado.getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("Template nao encontrado");
        assertThat(corpo).doesNotContain("Invalid api key");
    }

    @Test
    @DisplayName("deve devolver 404 ao buscar id inexistente")
    void naoEncontrado() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new com.teste.apis.exception.RecursoNaoEncontradoException("Contrato nao encontrado: 99"));

        mockMvc.perform(get("/contratos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /contratos/{id} deve devolver os dados do contrato")
    void buscaUm() throws Exception {
        when(service.buscarPorId(1L))
                .thenReturn(new ContratoResponseDTO(1L, "Lucas Neves", StatusContrato.GERADO,
                        "https://cdn.exemplo/contrato.pdf", OffsetDateTime.now()));

        mockMvc.perform(get("/contratos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("GERADO"));
    }
}
