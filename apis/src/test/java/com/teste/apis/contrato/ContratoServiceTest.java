package com.teste.apis.contrato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.teste.apis.exception.IntegrationException;
import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.integration.apitemplate.ApiTemplateClient;
import com.teste.apis.integration.apitemplate.dto.ApiTemplatePdfResponse;

/**
 * Testes do fluxo do contrato com os clients mockados:
 * PENDENTE -> GERADO e PENDENTE -> FALHA.
 */
@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

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

    @Mock
    private ContratoRepository repository;

    @Mock
    private ApiTemplateClient apiTemplateClient;

    private ContratoService service() {
        return new ContratoService(repository, apiTemplateClient);
    }

    @Test
    @DisplayName("deve salvar como PENDENTE e depois marcar GERADO com o link do provedor")
    void geraComSucesso() {
        List<StatusContrato> statusAoSalvar = new ArrayList<>();
        when(repository.save(any(Contrato.class))).thenAnswer(invocacao -> {
            Contrato c = invocacao.getArgument(0);
            statusAoSalvar.add(c.getStatus());
            return c;
        });

        when(apiTemplateClient.gerarPdf(REQUEST))
                .thenReturn(new ApiTemplatePdfResponse("success",
                        "https://cdn.exemplo/contrato.pdf", "ref-123", "template-fake", null));

        ContratoResponseDTO resposta = service().criar(REQUEST);

        assertThat(statusAoSalvar).containsExactly(StatusContrato.PENDENTE, StatusContrato.GERADO);
        assertThat(resposta.status()).isEqualTo(StatusContrato.GERADO);
        assertThat(resposta.pdfUrl()).isEqualTo("https://cdn.exemplo/contrato.pdf");
        assertThat(resposta.nomeCliente()).isEqualTo("Lucas Neves");
    }

    @Test
    @DisplayName("deve marcar como FALHA e repassar o erro quando o provedor falha")
    void marcaFalhaQuandoProvedorFalha() {
        when(repository.save(any(Contrato.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        when(apiTemplateClient.gerarPdf(REQUEST))
                .thenThrow(IntegrationException.falhaProvedor("APITemplate.io", "Template nao encontrado", 400));

        assertThatThrownBy(() -> service().criar(REQUEST))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("Template nao encontrado");

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(StatusContrato.FALHA);
    }

    @Test
    @DisplayName("deve marcar como FALHA quando o provedor nao devolve o link do PDF")    void marcaFalhaQuandoNaoTemLink() {
        when(repository.save(any(Contrato.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        when(apiTemplateClient.gerarPdf(REQUEST))
                .thenReturn(new ApiTemplatePdfResponse("success", null, "ref-123", "template-fake", null));

        assertThatThrownBy(() -> service().criar(REQUEST))
                .isInstanceOf(IntegrationException.class);

        ArgumentCaptor<Contrato> captor = ArgumentCaptor.forClass(Contrato.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getStatus()).isEqualTo(StatusContrato.FALHA);
    }

    @Test
    @DisplayName("deve devolver 404 ao buscar um contrato inexistente")
    void buscaInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("nao deve chamar o provedor ao apenas buscar ou listar")
    void listagemNaoChamaProvedor() {
        Contrato contrato = Contrato.novo(REQUEST, OffsetDateTime.now());
        when(repository.findAllByOrderByCriadoEmDesc()).thenReturn(List.of(contrato));

        assertThat(service().listar()).hasSize(1);
        verify(apiTemplateClient, never()).gerarPdf(any());
    }
}
