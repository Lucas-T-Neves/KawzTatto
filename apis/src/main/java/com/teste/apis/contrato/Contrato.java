package com.teste.apis.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contrato persistido no banco do projeto. A entity nunca aparece em
 * assinatura de controller: entra e sai sempre como DTO.
 */
@Getter
@Setter
@Entity
@Table(name = "contratos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomeCliente;

    @Column(nullable = false, length = 14)
    private String cpfCliente;

    @Column(nullable = false, length = 20)
    private String telefoneCliente;

    @Column(nullable = false, length = 150)
    private String nomeArtista;

    @Column(nullable = false, length = 1000)
    private String descricaoTatuagem;

    @Column(nullable = false, length = 100)
    private String localCorpo;

    @Column(nullable = false, length = 20)
    private String tamanhoEstimado;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorOrcado;

    @Column(nullable = false)
    private boolean sinalPago;

    @Column(nullable = false, length = 60)
    private String formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContrato status;

    @Column(length = 1000)
    private String pdfUrl;

    @Column(length = 100)
    private String transactionRef;

    @Column(nullable = false)
    private OffsetDateTime criadoEm;

    /** Cria o contrato ja com status PENDENTE e data de criacao preenchida. */
    public static Contrato novo(ContratoRequestDTO req, OffsetDateTime agora) {
        Contrato c = new Contrato();
        c.nomeCliente = req.nomeCliente();
        c.cpfCliente = req.cpfCliente();
        c.telefoneCliente = req.telefoneCliente();
        c.nomeArtista = req.nomeArtista();
        c.descricaoTatuagem = req.descricaoTatuagem();
        c.localCorpo = req.localCorpo();
        c.tamanhoEstimado = req.tamanhoEstimado();
        c.dataInicio = req.dataInicio();
        c.dataFim = req.dataFim();
        c.valorOrcado = req.valorOrcado();
        c.sinalPago = Boolean.TRUE.equals(req.sinalPago());
        c.formaPagamento = req.formaPagamento();
        c.status = StatusContrato.PENDENTE;
        c.criadoEm = agora;
        return c;
    }

    public void marcarGerado(String url, String transactionRef) {
        this.pdfUrl = url;
        this.transactionRef = transactionRef;
        this.status = StatusContrato.GERADO;
    }

    public void marcarFalha() {
        this.status = StatusContrato.FALHA;
    }
}
