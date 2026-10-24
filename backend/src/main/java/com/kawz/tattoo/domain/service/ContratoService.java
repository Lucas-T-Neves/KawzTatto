package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.AssinaturaInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoCreate;
import com.kawz.tattoo.api.dto.ApiModels.ContratoInput;
import com.kawz.tattoo.api.dto.ApiModels.ContratoResponse;
import com.kawz.tattoo.api.dto.ApiModels.ContratoViewResponse;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Contrato;
import com.kawz.tattoo.domain.repository.ContratoRepository;
import com.kawz.tattoo.domain.repository.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class ContratoService {
    private final ContratoRepository contratos;
    private final ProjetoRepository projetos;

    public ContratoService(ContratoRepository contratos, ProjetoRepository projetos) {
        this.contratos = contratos;
        this.projetos = projetos;
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

    @Transactional
    public ContratoResponse assinar(Integer id, AssinaturaInput input) {
        Contrato contrato = obter(id);
        contrato.registrarAssinatura(input.data() != null ? input.data() : LocalDate.now());
        return toResponse(contrato);
    }

    private Contrato obter(Integer id) {
        return contratos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }
}
