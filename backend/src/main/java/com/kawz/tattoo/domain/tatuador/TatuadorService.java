package com.kawz.tattoo.domain.tatuador;

import com.kawz.tattoo.api.tatuador.TatuadorMapper;
import com.kawz.tattoo.api.tatuador.dto.LoginRequest;
import com.kawz.tattoo.api.tatuador.dto.LoginResponse;
import com.kawz.tattoo.api.tatuador.dto.TatuadorCreate;
import com.kawz.tattoo.api.tatuador.dto.TatuadorResponse;
import com.kawz.tattoo.api.tatuador.dto.TatuadorUpdate;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.security.JwtService;
import com.kawz.tattoo.domain.security.SenhaEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Cadastro, autenticação e perfil do tatuador.
 *
 * <p>Leitura por id, atualização e exclusão são restritas ao próprio tatuador
 * autenticado: outro id responde 404. A listagem permanece como diretório do
 * estúdio (nome e e-mail dos artistas), sem expor senha.</p>
 */
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
        // Page.map transforma cada Tatuador em TatuadorResponse e conserva
        // os dados de paginação produzidos pelo repository.
        return repository.findAll(pageable).map(TatuadorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TatuadorResponse buscar(Integer id, Integer tatuadorId) {
        return TatuadorMapper.toResponse(obterProprio(id, tatuadorId));
    }

    @Transactional(readOnly = true)
    public TatuadorResponse buscarPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .map(TatuadorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador", email));
    }

    @Transactional
    public TatuadorResponse criar(TatuadorCreate input) {
        String email = normalizarEmail(input.email());
        if (repository.existsByEmailIgnoreCase(email)) throw new BusinessException("E-mail já cadastrado");
        return TatuadorMapper.toResponse(repository.save(new Tatuador(
                input.nome().trim(), email, senhaEncoder.codificar(input.senha()))));
    }

    @Transactional
    public TatuadorResponse atualizar(Integer id, TatuadorUpdate input, Integer tatuadorId) {
        Tatuador tatuador = obterProprio(id, tatuadorId);
        String email = normalizarEmail(input.email());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessException("E-mail já cadastrado");
        }
        tatuador.atualizar(input.nome().trim(), email);
        if (input.senha() != null && !input.senha().isBlank()) {
            tatuador.alterarSenha(senhaEncoder.codificar(input.senha()));
        }
        return TatuadorMapper.toResponse(tatuador);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        repository.delete(obterProprio(id, tatuadorId));
    }

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

    private Tatuador obterProprio(Integer id, Integer tatuadorId) {
        if (!id.equals(tatuadorId)) {
            throw new ResourceNotFoundException("Tatuador", id);
        }
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tatuador", id));
    }

    private String normalizarEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
