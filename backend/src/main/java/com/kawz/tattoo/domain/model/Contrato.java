package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato")
public class Contrato extends BaseEntity {
    @Column(name = "limite_alteracoes_arte", length = 45)
    private String limiteAlteracoesArte;
    @Column(name = "tolerancia_atraso", length = 45)
    private String toleranciaAtraso;
    @Column(name = "uso_imagem", length = 45)
    private String usoImagem;
    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;
    @Column(name = "status_contrato", length = 45)
    private String statusContrato;
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;
    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    protected Contrato() {}

    /** Todo contrato nasce como RASCUNHO; depois vira GERADO (PDF) e ASSINADO. */
    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (statusContrato == null) statusContrato = "RASCUNHO";
    }

    public void atualizar(String limiteAlteracoesArte, String toleranciaAtraso, String usoImagem) {
        this.limiteAlteracoesArte = limiteAlteracoesArte;
        this.toleranciaAtraso = toleranciaAtraso;
        this.usoImagem = usoImagem;
    }

    public void registrarAssinatura(LocalDate data) {
        this.dataAssinatura = data;
        this.statusContrato = "ASSINADO";
    }

    /** Guarda o PDF gerado no APITemplate. */
    public void registrarPdf(String pdfUrl, String transactionRef) {
        this.pdfUrl = pdfUrl;
        this.transactionRef = transactionRef;
        if (!"ASSINADO".equals(statusContrato)) this.statusContrato = "GERADO";
    }

    public String getLimiteAlteracoesArte() { return limiteAlteracoesArte; }
    public String getToleranciaAtraso() { return toleranciaAtraso; }
    public String getUsoImagem() { return usoImagem; }
    public LocalDate getDataAssinatura() { return dataAssinatura; }
    public String getStatusContrato() { return statusContrato; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public String getPdfUrl() { return pdfUrl; }
    public String getTransactionRef() { return transactionRef; }
}
