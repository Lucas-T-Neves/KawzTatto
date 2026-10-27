package com.kawz.tattoo.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String nome;
    @Column(length = 20)
    private String telefone;
    @Column(length = 20)
    private String cpf;
    @Column(length = 100)
    private String instagram;
    @Column(name = "canal_aquisicao", length = 45)
    private String canalAquisicao;
    @Column(name = "link_conversa", length = 255)
    private String linkConversa;
    @Column(name = "link_pasta", length = 255)
    private String linkPasta;
    @Column(length = 120)
    private String localizacao;
    @Column(length = 45)
    private String tamanho;
    @Column(length = 10000)
    private String ideia;
    @Column(length = 45)
    private String status;
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    // 1 cliente -> 1 ou muitas anamneses (correção sobre o DER: FK mora na anamnese)
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Anamnese> anamneses = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "preferencias_cliente_id", unique = true)
    private PreferenciasCliente preferencias;

    protected Cliente() {}

    public Cliente(String nome, String telefone, String cpf, String instagram, String canalAquisicao,
                   String linkConversa, String linkPasta, String localizacao,
                   String tamanho, String ideia, String status) {
        atualizarCadastro(nome, telefone, cpf, instagram, canalAquisicao, linkConversa, linkPasta,
                localizacao, tamanho, ideia, status);
        this.anamneses.add(new Anamnese(this, null, null));
        this.preferencias = new PreferenciasCliente();
    }

    @PrePersist
    void prePersist() {
        if (dataCriacao == null) dataCriacao = LocalDateTime.now();
    }

    public void atualizarCadastro(String nome, String telefone, String cpf, String instagram,
                                  String canalAquisicao, String linkConversa, String linkPasta,
                                  String localizacao, String tamanho,
                                  String ideia, String status) {
        this.nome = nome;
        this.telefone = telefone;
        this.cpf = cpf;
        this.instagram = instagram;
        this.canalAquisicao = canalAquisicao;
        this.linkConversa = linkConversa;
        this.linkPasta = linkPasta;
        this.localizacao = localizacao;
        this.tamanho = tamanho;
        this.ideia = ideia;
        this.status = status;
    }

    public Anamnese adicionarAnamnese(String dadosSaude, String preparacaoTatuagem) {
        Anamnese anamnese = new Anamnese(this, dadosSaude, preparacaoTatuagem);
        anamneses.add(anamnese);
        return anamnese;
    }

    public void removerAnamnese(Anamnese anamnese) {
        anamneses.remove(anamnese);
    }

    public void converterEmCliente() { this.status = "CLIENTE"; }

    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getCpf() { return cpf; }
    public String getInstagram() { return instagram; }
    public String getCanalAquisicao() { return canalAquisicao; }
    public String getLinkConversa() { return linkConversa; }
    public String getLinkPasta() { return linkPasta; }
    public String getLocalizacao() { return localizacao; }
    public String getTamanho() { return tamanho; }
    public String getIdeia() { return ideia; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public List<Anamnese> getAnamneses() { return anamneses; }
    public PreferenciasCliente getPreferencias() { return preferencias; }
}
