package com.kawz.tattoo.domain.anamnese;

import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "anamnese")
public class Anamnese extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(name = "logo_url", length = 1000) private String logoUrl;
    @Column(length = 150) private String nome;
    @Column(name = "data_nascimento") private LocalDate dataNascimento;
    @Column(length = 120) private String ocupacao;
    @Column(length = 20) private String telefone;
    @Column private Boolean whatsapp;
    @Column(length = 500) private String endereco;
    @Column(length = 30) private String rg;
    @Column(length = 20) private String cpf;
    @Lob @Column(name = "saude_json") private String saudeJson;
    @Column(name = "data_assinatura") private LocalDate dataAssinatura;
    @Column(name = "assinatura_url", length = 1000) private String assinaturaUrl;
    @Lob @Column(name = "procedimento_json") private String procedimentoJson;
    @Column(name = "pdf_url", length = 1000) private String pdfUrl;
    @Column(name = "transaction_ref", length = 100) private String transactionRef;
    @Column(name = "documento_assinado_url", length = 1000) private String documentoAssinadoUrl;
    @Column(name = "documento_assinado_public_id", length = 255) private String documentoAssinadoPublicId;
    @Column(name = "documento_assinado_nome", length = 255) private String documentoAssinadoNome;
    @Column(name = "documento_assinado_mime", length = 100) private String documentoAssinadoMime;
    @Column(name = "documento_assinado_bytes") private Long documentoAssinadoBytes;
    @Column(name = "documento_assinado_em") private Instant documentoAssinadoEm;
    @Column(name = "dados_saude", length = 45) private String dadosSaude;
    @Column(name = "preparacao_tatuagem", length = 45) private String preparacaoTatuagem;

    protected Anamnese() {}

    public Anamnese(Cliente cliente, String dadosSaude, String preparacaoTatuagem) {
        this.cliente = cliente;
        this.dadosSaude = dadosSaude;
        this.preparacaoTatuagem = preparacaoTatuagem;
    }

    public void preencher(Cliente cliente, String logoUrl, String nome, LocalDate dataNascimento,
                          String ocupacao, String telefone, Boolean whatsapp, String endereco,
                          String rg, String cpf, String saudeJson, LocalDate dataAssinatura,
                          String assinaturaUrl, String procedimentoJson, String dadosSaude,
                          String preparacaoTatuagem) {
        this.cliente = cliente;
        this.logoUrl = logoUrl;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.ocupacao = ocupacao;
        this.telefone = telefone;
        this.whatsapp = whatsapp;
        this.endereco = endereco;
        this.rg = rg;
        this.cpf = cpf;
        this.saudeJson = saudeJson;
        this.dataAssinatura = dataAssinatura;
        this.assinaturaUrl = assinaturaUrl;
        this.procedimentoJson = procedimentoJson;
        this.dadosSaude = dadosSaude;
        this.preparacaoTatuagem = preparacaoTatuagem;
    }

    public Cliente getCliente() { return cliente; }
    public String getLogoUrl() { return logoUrl; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getOcupacao() { return ocupacao; }
    public String getTelefone() { return telefone; }
    public Boolean getWhatsapp() { return whatsapp; }
    public String getEndereco() { return endereco; }
    public String getRg() { return rg; }
    public String getCpf() { return cpf; }
    public String getSaudeJson() { return saudeJson; }
    public LocalDate getDataAssinatura() { return dataAssinatura; }
    public String getAssinaturaUrl() { return assinaturaUrl; }
    public String getProcedimentoJson() { return procedimentoJson; }
    public String getPdfUrl() { return pdfUrl; }
    public String getTransactionRef() { return transactionRef; }
    public String getDocumentoAssinadoUrl() { return documentoAssinadoUrl; }
    public String getDocumentoAssinadoPublicId() { return documentoAssinadoPublicId; }
    public String getDocumentoAssinadoNome() { return documentoAssinadoNome; }
    public String getDocumentoAssinadoMime() { return documentoAssinadoMime; }
    public Long getDocumentoAssinadoBytes() { return documentoAssinadoBytes; }
    public Instant getDocumentoAssinadoEm() { return documentoAssinadoEm; }
    public String getDadosSaude() { return dadosSaude; }
    public String getPreparacaoTatuagem() { return preparacaoTatuagem; }

    public void registrarPdf(String pdfUrl, String transactionRef) {
        this.pdfUrl = pdfUrl;
        this.transactionRef = transactionRef;
    }

    public void registrarDocumentoAssinado(String url, String publicId, String nome, String mime,
                                           Long bytes, Instant enviadoEm) {
        this.documentoAssinadoUrl = url;
        this.documentoAssinadoPublicId = publicId;
        this.documentoAssinadoNome = nome;
        this.documentoAssinadoMime = mime;
        this.documentoAssinadoBytes = bytes;
        this.documentoAssinadoEm = enviadoEm;
    }

    public void removerDocumentoAssinado() {
        this.documentoAssinadoUrl = null;
        this.documentoAssinadoPublicId = null;
        this.documentoAssinadoNome = null;
        this.documentoAssinadoMime = null;
        this.documentoAssinadoBytes = null;
        this.documentoAssinadoEm = null;
    }
}
