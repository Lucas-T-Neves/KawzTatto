package com.kawz.tattoo.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "preferencias_cliente")
public class PreferenciasCliente extends BaseEntity {
    @Column(name = "musica_favorita", length = 45)
    private String musicaFavorita;
    @Column(name = "comida_favorita", length = 45)
    private String comidaFavorita;
    @Column(name = "estilo_tatuagem_favorita", length = 45)
    private String estiloTatuagemFavorita;
    @Column(name = "faixa_media_orcamento", precision = 10, scale = 2)
    private BigDecimal faixaMediaOrcamento;
    @Column(name = "faixa_orcamento", length = 120)
    private String faixaOrcamento;
    @Column(length = 255)
    private String habitos;
    @Column(name = "disponibilidade_horarios", length = 255)
    private String disponibilidadeHorarios;
    @Column(name = "observacoes_gerais", length = 10000)
    private String observacoesGerais;

    protected PreferenciasCliente() {}

    public void atualizarPreferencias(String musicaFavorita, String comidaFavorita, String estiloTatuagemFavorita,
                                      BigDecimal faixaMediaOrcamento, String faixaOrcamento, String habitos,
                                      String disponibilidadeHorarios, String observacoesGerais) {
        this.musicaFavorita = musicaFavorita;
        this.comidaFavorita = comidaFavorita;
        this.estiloTatuagemFavorita = estiloTatuagemFavorita;
        this.faixaMediaOrcamento = faixaMediaOrcamento;
        this.faixaOrcamento = faixaOrcamento;
        this.habitos = habitos;
        this.disponibilidadeHorarios = disponibilidadeHorarios;
        this.observacoesGerais = observacoesGerais;
    }

    public String getMusicaFavorita() { return musicaFavorita; }
    public String getComidaFavorita() { return comidaFavorita; }
    public String getEstiloTatuagemFavorita() { return estiloTatuagemFavorita; }
    public BigDecimal getFaixaMediaOrcamento() { return faixaMediaOrcamento; }
    public String getFaixaOrcamento() { return faixaOrcamento; }
    public String getHabitos() { return habitos; }
    public String getDisponibilidadeHorarios() { return disponibilidadeHorarios; }
    public String getObservacoesGerais() { return observacoesGerais; }
}
