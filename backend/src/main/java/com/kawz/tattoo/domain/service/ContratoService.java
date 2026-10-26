package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.AssinaturaInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoCreate;
import com.kawz.tattoo.api.dto.ApiModels.ContratoInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoPdfInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoResponse;
import com.kawz.tattoo.api.dto.ApiModels.ContratoViewResponse;
import com.kawz.tattoo.api.dto.ContratoViewProjection;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Contrato;
import com.kawz.tattoo.domain.repository.ContratoRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import com.kawz.tattoo.integration.ApiTemplateClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class ContratoService {
    private final ContratoRepository contratos;
    private final ProjetoRepository projetos;
    private final ApiTemplateClient apiTemplate;
    private final String nomeEstudio;
    private final String documentoEstudio;

    public ContratoService(ContratoRepository contratos, ProjetoRepository projetos, ApiTemplateClient apiTemplate,
                           @Value("${kawz.estudio.nome:Kawz Tattoo}") String nomeEstudio,
                           @Value("${kawz.estudio.documento:}") String documentoEstudio) {
        this.contratos = contratos;
        this.projetos = projetos;
        this.apiTemplate = apiTemplate;
        this.nomeEstudio = nomeEstudio;
        this.documentoEstudio = documentoEstudio;
    }

    /** Leitura paginada pela view vw_contratos (cliente, artista, datas, sinal pago, termos). */
    @Transactional(readOnly = true)
    public Page<ContratoViewResponse> listar(Pageable pageable) {
        return contratos.buscarNaView(pageable)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContratoViewResponse buscar(Integer id) {
        return contratos.buscarNaViewPorId(id)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }

    @Transactional
    public ContratoResponse atualizar(Integer id, ContratoInput input) {
        Contrato contrato = obter(id);
        contrato.atualizar(input.limiteAlteracoesArte(), input.toleranciaAtraso(), input.usoImagem());
        return toResponse(contrato);
    }

    /** Preenche o contrato vazio criado automaticamente junto ao projeto. */
    @Transactional
    public ContratoResponse criar(ContratoCreate input) {
        Contrato contrato = projetos.findById(input.projetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", input.projetoId()))
                .getContrato();
        contrato.atualizar(input.limiteAlteracoesArte(), input.toleranciaAtraso(), input.usoImagem());
        return toResponse(contrato);
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscarPorProjeto(Integer projetoId) {
        var projeto = projetos.findById(projetoId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", projetoId));
        return toResponse(projeto.getContrato());
    }

    @Transactional
    public ContratoResponse assinar(Integer id, AssinaturaInput input) {
        Contrato contrato = obter(id);
        contrato.registrarAssinatura(input.data() != null ? input.data() : LocalDate.now());
        return toResponse(contrato);
    }

    /**
     * Gera o PDF do contrato no APITemplate com os dados da view vw_contratos e grava
     * pdf_url/transaction_ref. As chaves do mapa precisam bater com os placeholders do template.
     * Se o APITemplate falhar, nada é gravado (502).
     */
    @Transactional
    public ContratoResponse gerarPdf(Integer id, ContratoPdfInput extra) {
        Contrato contrato = obter(id);
        ContratoViewProjection v = contratos.buscarNaViewPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome_estudio", nomeEstudio);
        dados.put("documento_estudio", documentoEstudio);
        dados.put("nome_cliente", v.getNomeCliente());
        String cpf = v.getCpfCliente() != null && !v.getCpfCliente().isBlank()
                ? v.getCpfCliente() : (extra == null ? "" : texto(extra.cpfCliente()));
        dados.put("cpf_cliente", cpf);
        dados.put("telefone_cliente", texto(v.getTelefoneCliente()));
        dados.put("nome_artista", v.getNomeArtista());
        dados.put("descricao_tatuagem", texto(v.getDescricaoTatuagem()));
        dados.put("local_corpo", texto(v.getLocalCorpo()));
        dados.put("tamanho_estimado", extra == null ? "" : texto(extra.tamanhoEstimado()));
        dados.put("data_inicio", data(v.getDataInicio()));
        dados.put("data_fim", data(v.getDataFim()));
        dados.put("valor_orcado", dinheiro(v.getValorOrcado()));
        dados.put("sinal_pago", Boolean.TRUE.equals(com.kawz.tattoo.api.dto.ApiMapper.paraBoolean(v.getSinalPago())) ? "Sim" : "Não");
        dados.put("forma_pagamento", texto(v.getFormaPagamento()));
        dados.put("data_geracao", data(LocalDate.now()));

        ApiTemplateClient.PdfGerado pdf = apiTemplate.gerarContrato(dados);
        contrato.registrarPdf(pdf.downloadUrl(), pdf.transactionRef());
        return toResponse(contrato);
    }

    private static String texto(String valor) { return valor == null ? "" : valor; }

    private static String data(LocalDate valor) {
        return valor == null ? "" : valor.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static String dinheiro(BigDecimal valor) {
        return valor == null ? "" : String.format(Locale.of("pt", "BR"), "R$ %,.2f", valor);
    }

    private Contrato obter(Integer id) {
        return contratos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }
}
