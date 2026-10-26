package com.kawz.tattoo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class ApiModels {
    private ApiModels() {}

    public record TatuadorCreate(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String senha) {}

    public record TatuadorUpdate(
            @NotBlank @Size(max = 100) String nome,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(min = 8, max = 72) String senha) {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}
    public record LoginResponse(boolean autenticado, Integer tatuadorId, String nome,
                                String token, LocalDateTime expiraEm) {}
    public record TatuadorResponse(Integer id, String nome, String email) {}
    public record ConversaResponse(Integer clienteId, String url, String status) {}

    public record ClienteInput(
            @NotBlank @Size(max = 150) String nome,
            @Size(max = 20) String telefone,
            @Size(max = 20) String cpf,
            @Size(max = 100) String instagram,
            @Size(max = 45) String canalAquisicao,
            @Size(max = 255) String linkConversa,
            @Size(max = 255) String linkPasta,
            @NotBlank @Size(max = 45) String status) {}

    public record ClienteResponse(Integer id, String nome, String telefone, String cpf, String instagram,
                                  String canalAquisicao, String linkConversa, String linkPasta,
                                  String status, LocalDateTime dataCriacao) {}

    public record ProjetoInput(
            @NotNull Integer tatuadorId,
            @NotNull Integer clienteId,
            @Size(max = 150) String nome,
            @NotBlank @Size(max = 45) String descricao,
            @Size(max = 255) String linkPasta,
            @NotBlank @Size(max = 45) String statusProjeto,
            @NotNull @DecimalMin("0.00") BigDecimal valorTotal,
            @NotNull @DecimalMin("0.00") BigDecimal valorSinal,
            @NotNull @Min(1) Integer quantidadeSessoes,
            LocalDate dataValidade,
            @Size(max = 45) String lembretePre,
            @Size(max = 45) String lembretePost,
            @Size(max = 10000) String ideiaExpectativa,
            @Size(max = 45) String estiloArte,
            @Size(max = 45) String localizacaoCorpo,
            @Size(max = 45) String tamanho,
            @Size(max = 45) String dificuldade,
            @Size(max = 45) String cores,
            @Size(max = 45) String complexidade) {}

    /**
     * Saldo = valorTotal - recebido (quanto falta receber).
     * Recebido = soma das movimentações de ENTRADA com status "pago";
     * SinalPago = existe movimentação de sinal paga — mesma regra da view vw_contratos.
     */
    public record ProjetoResponse(Integer id, Integer tatuadorId, Integer clienteId, String nome, String descricao,
                                  String linkPasta,
                                  String statusProjeto, BigDecimal valorTotal, BigDecimal valorSinal,
                                  BigDecimal saldo, BigDecimal recebido, Boolean sinalPago,
                                  Integer quantidadeSessoes, LocalDate dataValidade,
                                  String lembretePre, String lembretePost,
                                  String ideiaExpectativa, String estiloArte, String localizacaoCorpo,
                                  String tamanho, String dificuldade, String cores, String complexidade,
                                  Integer contratoId, Integer caracteristicasProjetoId) {}

    public record ProjetoDetalhadoResponse(ProjetoResponse projeto,
                                           java.util.List<SessaoResponse> sessoes,
                                           java.util.List<MovimentacaoResponse> movimentacoes,
                                           java.util.List<FollowUpResponse> followUps,
                                           java.util.List<ImagemResponse> imagens,
                                           CaracteristicasProjetoResponse caracteristicas,
                                           ContratoResponse contrato) {}

    public record StatusRequest(@NotBlank @Size(max = 45) String status) {}

    public record SessaoInput(
            @NotNull Integer projetoId,
            @NotNull LocalDateTime dataHorario,
            @NotBlank @Size(max = 45) String localAtendimento,
            @NotBlank @Size(max = 45) String statusSessao,
            @Size(max = 45) String observacoesSessao) {}

    public record SessaoRemarcarInput(@NotNull LocalDateTime dataHorario) {}

    public record SessaoResponse(Integer id, Integer projetoId, LocalDateTime dataHorario,
                                 String localAtendimento, String statusSessao, String observacoesSessao) {}

    public record MovimentacaoInput(
            @NotNull Integer projetoId,
            @NotBlank @Size(max = 45) @Pattern(regexp = "ENTRADA|SAIDA",
                    message = "deve ser ENTRADA ou SAIDA") String naturezaTransacao,
            @NotBlank @Size(max = 45) String categoria,
            LocalDate dataOcorrencia,
            @NotNull @DecimalMin("0.00") BigDecimal valorMonetario,
            @Size(max = 45) String formaPagamento,
            @NotBlank @Size(max = 45) @Pattern(regexp = "pago|pendente",
                    message = "deve ser pago ou pendente") String statusPagamento) {}

    public record MovimentacaoResponse(Integer id, Integer projetoId, String naturezaTransacao,
                                       String categoria, LocalDate dataOcorrencia, BigDecimal valorMonetario,
                                       String formaPagamento, String statusPagamento) {}

    public record MovimentacaoResumoResponse(String mes, BigDecimal receita, BigDecimal despesas,
                                              BigDecimal saldo, BigDecimal pendencias,
                                              BigDecimal receitasPendentes, BigDecimal despesasPendentes) {}

    public record AtividadeResponse(Integer id, Integer tatuadorId, String descricao, String tipo, String titulo, LocalDate data, LocalTime inicio, LocalTime termino, String local, String observacoes) {}

    public record FollowUpResponse(Integer id, Integer clienteId, String tipoContato, String statusContato,
                                   LocalDate dataPrevista, LocalDate dataRealizacao,
                                   String descricaoObservacao, String proximaAcao) {}

    public record AnamneseInput(@NotNull Integer clienteId,
                                @Size(max = 45) String dadosSaude,
                                @Size(max = 45) String preparacaoTatuagem) {}

    public record AnamneseResponse(Integer id, Integer clienteId, String dadosSaude,
                                   String preparacaoTatuagem) {}

    public record PreferenciasClienteInput(@Size(max = 45) String musicaFavorita,
                                           @Size(max = 45) String comidaFavorita,
                                           @Size(max = 45) String estiloTatuagemFavorita,
                                           @DecimalMin("0.00") BigDecimal faixaMediaOrcamento,
                                           @Size(max = 45) String habitos,
                                           @Size(max = 45) String disponibilidadeHorarios,
                                           @Size(max = 45) String observacoesGerais) {}

    public record PreferenciasClienteResponse(Integer id, Integer clienteId, String musicaFavorita,
                                              String comidaFavorita, String estiloTatuagemFavorita,
                                              BigDecimal faixaMediaOrcamento, String habitos,
                                              String disponibilidadeHorarios, String observacoesGerais) {}

    public record CaracteristicasProjetoInput(@Size(max = 10000) String ideiaExpectativa,
                                              @Size(max = 45) String estiloArte,
                                              @Size(max = 45) String localizacaoCorpo,
                                              @Size(max = 45) String tamanho,
                                              @Size(max = 45) String dificuldade,
                                              @Size(max = 45) String cores,
                                              @Size(max = 45) String complexidade) {}

    public record CaracteristicasProjetoResponse(Integer id, Integer projetoId, String ideiaExpectativa,
                                                 String estiloArte, String localizacaoCorpo, String tamanho,
                                                 String dificuldade, String cores, String complexidade) {}

    public record ImagemInput(@NotNull Integer projetoId,
                              @NotBlank @Size(max = 255) String publicId,
                              @NotBlank @Size(max = 1000) String url,
                              @Size(max = 20) String formato,
                              @Size(max = 100) String bytes,
                              Integer larguraAltura) {}

    public record ImagemResponse(Integer id, Integer projetoId, String publicId, String url,
                                 String formato, String bytes, Integer larguraAltura,
                                 LocalDateTime criadoEm) {}

    public record AtividadeInput(@NotNull Integer tatuadorId,
                                 @NotBlank @Size(max = 45) String descricao,
                                 @NotBlank @Size(max = 45) String tipo,
                                 @Size(max = 120) String titulo,
                                 LocalDate data,
                                 LocalTime inicio,
                                 LocalTime termino,
                                 @Size(max = 255) String local,
                                 @Size(max = 2000) String observacoes) {
        public AtividadeInput(Integer tatuadorId, String descricao, String tipo) {
            this(tatuadorId, descricao, tipo, descricao, null, null, null, null, null);
        }
    }

    public record FollowUpInput(@NotNull Integer clienteId,
                                @NotBlank @Size(max = 45) String tipoContato,
                                @NotBlank @Size(max = 45) String statusContato,
                                @NotNull LocalDate dataPrevista,
                                LocalDate dataRealizacao,
                                @Size(max = 10000) String descricaoObservacao,
                                @Size(max = 45) String proximaAcao) {}

    public record ContratoInput(
            @Size(max = 45) String limiteAlteracoesArte,
            @Size(max = 45) String toleranciaAtraso,
            @Size(max = 45) String usoImagem) {}

    public record ContratoCreate(@NotNull Integer projetoId,
                                 @Size(max = 45) String limiteAlteracoesArte,
                                 @Size(max = 45) String toleranciaAtraso,
                                 @Size(max = 45) String usoImagem) {}

    public record AssinaturaInput(LocalDate data) {}

    public record ContratoResponse(Integer id, String limiteAlteracoesArte, String toleranciaAtraso,
                                   String usoImagem, LocalDate dataAssinatura,
                                   String statusContrato, LocalDateTime criadoEm,
                                   String pdfUrl, String transactionRef) {}

    /** Dados que não existem no banco e entram no PDF do contrato (todos opcionais). */
    public record ContratoPdfInput(@Size(max = 20) String cpfCliente,
                                   @Size(max = 100) String tamanhoEstimado) {}

    public record ContratoViewResponse(Integer contratoId, String nomeCliente, String telefoneCliente,
                                       String cpfCliente, String nomeArtista, String descricaoTatuagem, String localCorpo,
                                       LocalDate dataInicio, LocalDate dataFim, BigDecimal valorOrcado,
                                       Boolean sinalPago, String formaPagamento, String limiteAlteracoesArte,
                                       String toleranciaAtraso, String usoImagem, LocalDate dataAssinatura,
                                       String statusContrato, String pdfUrl) {}

    public record DashboardResponse(String mes, long projetosAtivos, long sessoesHoje,
                                    long followUpsPendentes, BigDecimal receitaMes,
                                    BigDecimal despesasMes, BigDecimal saldoMes,
                                    BigDecimal pendenciasMes) {}
}
