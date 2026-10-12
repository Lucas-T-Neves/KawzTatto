package com.teste.apis.integration.apitemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.teste.apis.contrato.ContratoRequestDTO;
import com.teste.apis.contrato.EstudioProperties;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.integration.apoio.ErroProvedor;
import com.teste.apis.integration.apoio.ProvedorHttp;
import com.teste.apis.integration.apitemplate.dto.ApiTemplatePdfResponse;

/**
 * Client do APITemplate.io. Concentra tudo que e especifico do provedor:
 * endpoint, autenticacao, formato do corpo e o mapeamento dominio -> template.
 *
 * <p>O tratamento de erro (marcar 4xx/5xx, ler corpo, traduzir para
 * IntegrationException) fica no {@link ProvedorHttp} — la, uma vez so.</p>
 */
@Component
public class ApiTemplateClient {

    private static final Logger log = LoggerFactory.getLogger(ApiTemplateClient.class);
    private static final String PROVEDOR = "APITemplate.io";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;
    private final ApiTemplateProperties props;
    private final EstudioProperties estudio;

    public ApiTemplateClient(RestClient apitemplateRestClient,
                             ApiTemplateProperties props,
                             EstudioProperties estudio) {
        this.restClient = apitemplateRestClient;
        this.props = props;
        this.estudio = estudio;
    }

    /** Gera o PDF a partir dos dados do contrato e devolve a resposta do provedor. */
    public ApiTemplatePdfResponse gerarPdf(ContratoRequestDTO request) {
        if (props.getTemplateId() == null || props.getTemplateId().isBlank()) {
            log.error("template_id do APITemplate nao configurado: defina a variavel APITEMPLATE_TEMPLATE_ID");
            throw IntegrationException.falhaProvedor(PROVEDOR,
                    "template_id do APITemplate nao configurado no servidor", null);
        }

        Map<String, Object> corpo = mapearParaTemplate(request);

        try {
            return ProvedorHttp.comTratamentoDeErro(
                    restClient.post()
                            .uri(uri -> uri.path("/create-pdf")
                                    .queryParam("template_id", props.getTemplateId())
                                    .build())
                            .header("X-API-KEY", props.getApiKey())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(corpo)
                            .retrieve())
                    .body(ApiTemplatePdfResponse.class);
        } catch (ErroProvedor e) {
            throw ProvedorHttp.traduzir(e, PROVEDOR);
        } catch (ResourceAccessException e) {
            log.error("Timeout ou falha de rede ao chamar provedor={}", PROVEDOR);
            throw ProvedorHttp.timeout(PROVEDOR, "Tempo esgotado ao gerar o PDF");
        }
    }

    /**
     * Mapeamento unico dominio -> variaveis do template (secao 13.2).
     * Fica no client porque os nomes em snake_case sao formato do provedor
     * e nao devem vazar para o dominio.
     */
    Map<String, Object> mapearParaTemplate(ContratoRequestDTO request) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nome_estudio", estudio.getNome());
        m.put("nome_cliente", request.nomeCliente());
        m.put("cpf_cliente", request.cpfCliente());
        m.put("telefone_cliente", request.telefoneCliente());
        m.put("nome_artista", request.nomeArtista());
        m.put("documento_estudio", estudio.getDocumento());
        m.put("descricao_tatuagem", request.descricaoTatuagem());
        m.put("local_corpo", request.localCorpo());
        m.put("tamanho_estimado", request.tamanhoEstimado());
        m.put("data_inicio", formatarData(request.dataInicio()));
        m.put("data_fim", formatarData(request.dataFim()));
        m.put("valor_orcado", request.valorOrcado());
        m.put("sinal_pago", Boolean.TRUE.equals(request.sinalPago()) ? "Sim" : "Nao");
        m.put("forma_pagamento", request.formaPagamento());
        m.put("data_geracao", formatarData(LocalDate.now(FUSO)));
        return m;
    }

    private String formatarData(LocalDate data) {
        return data == null ? null : DATA_BR.format(data);
    }
}
