package com.kawz.tattoo.api.dto;

import com.kawz.tattoo.domain.model.Cliente;
import com.kawz.tattoo.domain.model.Contrato;
import com.kawz.tattoo.domain.model.Atividade;
import com.kawz.tattoo.domain.model.Anamnese;
import com.kawz.tattoo.domain.model.CaracteristicasProjeto;
import com.kawz.tattoo.domain.model.FollowUp;
import com.kawz.tattoo.domain.model.Imagem;
import com.kawz.tattoo.domain.model.MovimentacaoFinanceira;
import com.kawz.tattoo.domain.model.Orcamento;
import com.kawz.tattoo.domain.model.PreferenciasCliente;
import com.kawz.tattoo.domain.model.Projeto;
import com.kawz.tattoo.domain.model.Sessao;
import com.kawz.tattoo.domain.model.Tatuador;

import java.math.BigDecimal;

import static com.kawz.tattoo.api.dto.ApiModels.*;

public final class ApiMapper {
    private ApiMapper() {}

    public static TatuadorResponse toResponse(Tatuador value) {
        return new TatuadorResponse(value.getId(), value.getNome(), value.getEmail());
    }

    public static ClienteResponse toResponse(Cliente value) {
        PreferenciasCliente preferencias = value.getPreferencias();
        return new ClienteResponse(value.getId(), value.getNome(), value.getTelefone(),
                value.getCpf(), value.getInstagram(), value.getCanalAquisicao(), value.getLinkConversa(), value.getLinkPasta(),
                value.getLocalizacao(), value.getTamanho(), value.getIdeia(),
                preferencias == null ? null : preferencias.getMusicaFavorita(),
                preferencias == null ? null : preferencias.getComidaFavorita(),
                preferencias == null ? null : preferencias.getEstiloTatuagemFavorita(),
                preferencias == null ? null : preferencias.getFaixaOrcamento(),
                preferencias == null ? null : preferencias.getHabitos(),
                preferencias == null ? null : preferencias.getDisponibilidadeHorarios(),
                preferencias == null ? null : preferencias.getObservacoesGerais(),
                value.getStatus(), value.getDataCriacao());
    }

    /**
     * Saldo = valorTotal - recebido (quanto falta receber); recebido e sinalPago
     * vêm das movimentações financeiras do projeto (regra da view vw_contratos).
     */
    public static ProjetoResponse toResponse(Projeto value, BigDecimal recebido, boolean sinalPago, Imagem referencia) {
        var caracteristicas = value.getCaracteristicas();
        return new ProjetoResponse(value.getId(), value.getTatuador().getId(), value.getCliente().getId(),
                value.getNome(), value.getDescricao(), value.getLinkPasta(),
                value.getStatusProjeto(), value.getValorTotal(), value.getValorSinal(),
                value.calcularSaldo(recebido), recebido, sinalPago, value.getQuantidadeSessoes(),
                value.getDataValidade(), value.getLembretePre(), value.getLembretePost(),
                caracteristicas.getIdeiaExpectativa(), caracteristicas.getEstiloArte(),
                caracteristicas.getLocalizacaoCorpo(), caracteristicas.getTamanho(),
                caracteristicas.getDificuldade(), caracteristicas.getCores(), caracteristicas.getComplexidade(),
                referencia == null ? null : referencia.getId(),
                referencia == null ? null : nomeImagem(referencia),
                referencia == null ? null : referencia.getUrl(),
                value.getContrato().getId(), caracteristicas.getId());
    }

    public static SessaoResponse toResponse(Sessao value) {
        return new SessaoResponse(value.getId(), value.getProjeto().getId(), value.getDataHorario(),
                value.getLocalAtendimento(), value.getStatusSessao(), value.getObservacoesSessao());
    }

    public static MovimentacaoResponse toResponse(MovimentacaoFinanceira value) {
        return new MovimentacaoResponse(value.getId(), value.getProjeto().getId(), value.getNaturezaTransacao(),
                value.getCategoria(), value.getDataOcorrencia(), value.getValorMonetario(),
                value.getFormaPagamento(), value.getStatusPagamento());
    }

    public static OrcamentoResponse toResponse(Orcamento value) {
        return new OrcamentoResponse(value.getId(), value.getProjeto().getId(), value.getCliente().getId(),
                value.getNome(), value.getDataOrcamento(), value.getStatus(), value.getValorTotal(),
                value.getValorSinal(), value.getQuantidadeSessoes(), value.valorSessaoEstimado(),
                value.getFormaPagamento(), value.getDataValidade(), value.getPropostaAceita());
    }

    public static AtividadeResponse toResponse(Atividade value) {
        return new AtividadeResponse(value.getId(), value.getTatuador().getId(), value.getProjetoId(),
                value.getClienteId(), value.getDescricao(), value.getTipo(), value.getStatus(),
                value.getTitulo(), value.getData(), value.getInicio(), value.getTermino(),
                value.getLocal(), value.getObservacoes());
    }

    public static FollowUpResponse toResponse(FollowUp value) {
        return new FollowUpResponse(value.getId(), value.getCliente().getId(), value.getTipoContato(),
                value.getStatusContato(), value.getDataPrevista(), value.getDataRealizacao(),
                value.getDescricaoObservacao(), value.getProximaAcao());
    }

    public static AnamneseResponse toResponse(Anamnese value) {
        return new AnamneseResponse(value.getId(), value.getCliente().getId(), value.getDadosSaude(),
                value.getPreparacaoTatuagem());
    }

    public static PreferenciasClienteResponse toResponse(PreferenciasCliente value, Integer clienteId) {
        return new PreferenciasClienteResponse(value.getId(), clienteId, value.getMusicaFavorita(),
                value.getComidaFavorita(), value.getEstiloTatuagemFavorita(), value.getFaixaMediaOrcamento(),
                value.getFaixaOrcamento(),
                value.getHabitos(), value.getDisponibilidadeHorarios(), value.getObservacoesGerais());
    }

    public static CaracteristicasProjetoResponse toResponse(CaracteristicasProjeto value, Integer projetoId) {
        return new CaracteristicasProjetoResponse(value.getId(), projetoId, value.getIdeiaExpectativa(),
                value.getEstiloArte(), value.getLocalizacaoCorpo(), value.getTamanho(), value.getDificuldade(),
                value.getCores(), value.getComplexidade());
    }

    public static ImagemResponse toResponse(Imagem value) {
        return new ImagemResponse(value.getId(), value.getProjeto().getId(), value.getPublicId(), value.getUrl(),
                nomeImagem(value),
                value.getFormato(), value.getBytes(), value.getLarguraAltura(), value.getCriadoEm());
    }

    private static String nomeImagem(Imagem imagem) {
        String publicId = imagem.getPublicId();
        if (publicId == null || publicId.isBlank()) return null;
        int separador = publicId.lastIndexOf('/');
        String nome = separador >= 0 ? publicId.substring(separador + 1) : publicId;
        return nome.isBlank() ? publicId : nome;
    }

    public static ContratoResponse toResponse(Contrato value) {
        return new ContratoResponse(value.getId(), value.getLimiteAlteracoesArte(), value.getToleranciaAtraso(),
                value.getUsoImagem(), value.getDataAssinatura(),
                value.getStatusContrato(), value.getCriadoEm(), value.getPdfUrl(), value.getTransactionRef());
    }

    /** Converte o EXISTS da view (boolean no H2, 0/1 no MySQL) em Boolean. */
    public static Boolean paraBoolean(Object valor) {
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        return Boolean.FALSE;
    }

    public static ContratoViewResponse toResponse(ContratoViewProjection value) {
        return new ContratoViewResponse(value.getContratoId(), value.getNomeCliente(), value.getTelefoneCliente(),
                value.getCpfCliente(), value.getNomeArtista(), value.getDescricaoTatuagem(), value.getLocalCorpo(),
                value.getDataInicio(), value.getDataFim(), value.getValorOrcado(), paraBoolean(value.getSinalPago()),
                value.getFormaPagamento(), value.getLimiteAlteracoesArte(), value.getToleranciaAtraso(),
                value.getUsoImagem(), value.getDataAssinatura(), value.getStatusContrato(), value.getPdfUrl());
    }
}
