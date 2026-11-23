package com.kawz.tattoo.domain.contrato;

import com.kawz.tattoo.api.contrato.ContratoMapper;
import com.kawz.tattoo.api.contrato.dto.AssinaturaInput;
import com.kawz.tattoo.api.contrato.dto.ContratoCreate;
import com.kawz.tattoo.api.contrato.dto.ContratoInput;
import com.kawz.tattoo.api.contrato.dto.ContratoPdfInput;
import com.kawz.tattoo.api.contrato.dto.ContratoResponse;
import com.kawz.tattoo.api.contrato.dto.ContratoViewResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.projeto.Projeto;
import com.kawz.tattoo.domain.projeto.ProjetoRepository;
import com.kawz.tattoo.infrastructure.integration.ApiTemplateClient;
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

/**
 * Contratos dos projetos do tatuador autenticado. O contrato não guarda o dono:
 * a posse é sempre derivada do projeto (projeto.contrato_id + projeto.tatuador_id),
 * inclusive na view vw_contratos.
 */
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
    public Page<ContratoViewResponse> listar(Integer tatuadorId, Pageable pageable) {
        // Page.map converte cada projeção da view para ContratoViewResponse
        // preservando os metadados da página retornada pelo repository.
        return contratos.buscarNaView(tatuadorId, pageable).map(ContratoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContratoViewResponse buscar(Integer id, Integer tatuadorId) {
        // Optional.map simplifica a conversão do contrato encontrado para DTO
        // e mantém o Optional vazio para que o tratamento de 404 seja aplicado.
        return contratos.buscarNaViewPorId(id, tatuadorId)
                .map(ContratoMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }

    @Transactional
    public ContratoResponse atualizar(Integer id, ContratoInput input, Integer tatuadorId) {
        Contrato contrato = obterDoTatuador(id, tatuadorId);
        contrato.atualizar(input.limiteAlteracoesArte(), input.toleranciaAtraso(), input.usoImagem());
        return ContratoMapper.toResponse(contrato);
    }

    /** Preenche o contrato vazio criado automaticamente junto ao projeto. */
    @Transactional
    public ContratoResponse criar(ContratoCreate input, Integer tatuadorId) {
        Contrato contrato = obterProjetoDoTatuador(input.projetoId(), tatuadorId).getContrato();
        contrato.atualizar(input.limiteAlteracoesArte(), input.toleranciaAtraso(), input.usoImagem());
        return ContratoMapper.toResponse(contrato);
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscarPorProjeto(Integer projetoId, Integer tatuadorId) {
        return ContratoMapper.toResponse(obterProjetoDoTatuador(projetoId, tatuadorId).getContrato());
    }

    @Transactional
    public ContratoResponse assinar(Integer id, AssinaturaInput input, Integer tatuadorId) {
        Contrato contrato = obterDoTatuador(id, tatuadorId);
        contrato.registrarAssinatura(input.data() != null ? input.data() : LocalDate.now());
        return ContratoMapper.toResponse(contrato);
    }

    /**
     * Gera o PDF do contrato no APITemplate com os dados da view vw_contratos e grava
     * pdf_url/transaction_ref. As chaves do mapa precisam bater com os placeholders do template.
     * Se o APITemplate falhar, nada é gravado (502).
     */
    @Transactional
    public ContratoResponse gerarPdf(Integer id, ContratoPdfInput extra, Integer tatuadorId) {
        Contrato contrato = obterDoTatuador(id, tatuadorId);
        ContratoViewProjection v = contratos.buscarNaViewPorId(id, tatuadorId)
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
        dados.put("sinal_pago", Boolean.TRUE.equals(ContratoMapper.paraBoolean(v.getSinalPago())) ? "Sim" : "Não");
        dados.put("forma_pagamento", texto(v.getFormaPagamento()));
        dados.put("data_geracao", data(LocalDate.now()));

        ApiTemplateClient.PdfGerado pdf = apiTemplate.gerarContrato(dados);
        contrato.registrarPdf(pdf.downloadUrl(), pdf.transactionRef());
        return ContratoMapper.toResponse(contrato);
    }

    private static String texto(String valor) { return valor == null ? "" : valor; }

    private static String data(LocalDate valor) {
        return valor == null ? "" : valor.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static String dinheiro(BigDecimal valor) {
        return valor == null ? "" : String.format(Locale.of("pt", "BR"), "R$ %,.2f", valor);
    }

    private Contrato obterDoTatuador(Integer id, Integer tatuadorId) {
        // Optional.map extrai o contrato do projeto somente quando ele existe,
        // evitando acessar diretamente um valor possivelmente ausente.
        return projetos.findByContratoIdAndTatuadorId(id, tatuadorId)
                .map(Projeto::getContrato)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }

    private Projeto obterProjetoDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }
}
