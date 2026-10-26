package com.kawz.tattoo.domain.service;

import com.kawz.tattoo.api.dto.ApiModels.*;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.model.Tatuador;
import com.kawz.tattoo.domain.repository.TatuadorRepository;
import com.kawz.tattoo.domain.security.JwtService;
import com.kawz.tattoo.domain.security.SenhaEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static com.kawz.tattoo.api.dto.ApiMapper.toResponse;

@Service
public class TatuadorService {
    private final TatuadorRepository repository;
    private final SenhaEncoder senhaEncoder;
    private final JwtService jwtService;

    public TatuadorService(TatuadorRepository repository, SenhaEncoder senhaEncoder, JwtService jwtService) {
        this.repository = repository;
        this.senhaEncoder = senhaEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public Page<TatuadorResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(com.kawz.tattoo.api.dto.ApiMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TatuadorResponse buscar(Integer id) { return toResponse(obter(id)); }

    @Transactional(readOnly = true)
    public TatuadorResponse buscarPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .map(com.kawz.tattoo.api.dto.ApiMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador", email));
    }

    @Transactional
    public TatuadorResponse criar(TatuadorCreate input) {
        String email = normalizarEmail(input.email());
        if (repository.existsByEmailIgnoreCase(email)) throw new BusinessException("E-mail já cadastrado");
        return toResponse(repository.save(new Tatuador(
                input.nome().trim(), email, senhaEncoder.codificar(input.senha()))));
    }

    @Transactional
    public TatuadorResponse atualizar(Integer id, TatuadorUpdate input) {
        Tatuador tatuador = obter(id);
        String email = normalizarEmail(input.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("E-mail já cadastrado");
        }
        tatuador.atualizar(input.nome().trim(), email);
        if (input.senha() != null && !input.senha().isBlank()) {
            tatuador.alterarSenha(senhaEncoder.codificar(input.senha()));
        }
        return toResponse(tatuador);
    }

    @Transactional
    public void excluir(Integer id) { repository.delete(obter(id)); }

    /** Emite o JWT quando a senha confere; sem senha correta, autenticado=false e sem token. */
    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest input) {
        return repository.findByEmailIgnoreCase(normalizarEmail(input.email()))
                .filter(tatuador -> tatuador.autenticar(input.senha(), senhaEncoder))
                .map(tatuador -> {
                    JwtService.TokenEmissao emissao = jwtService.emitir(tatuador);
                    return new LoginResponse(true, tatuador.getId(), tatuador.getNome(),
                            emissao.token(), emissao.expiraEm());
                })
                .orElseGet(() -> new LoginResponse(false, null, null, null, null));
    }

    private Tatuador obter(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tatuador", id));
    }

    private String normalizarEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
