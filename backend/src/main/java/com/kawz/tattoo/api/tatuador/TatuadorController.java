package com.kawz.tattoo.api.tatuador;

import com.kawz.tattoo.api.tatuador.dto.LoginRequest;
import com.kawz.tattoo.api.tatuador.dto.LoginResponse;
import com.kawz.tattoo.api.tatuador.dto.TatuadorCreate;
import com.kawz.tattoo.api.tatuador.dto.TatuadorResponse;
import com.kawz.tattoo.api.tatuador.dto.TatuadorUpdate;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.tatuador.TatuadorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Diretório de tatuadores + dados do próprio perfil.
 *
 * <p>A listagem continua aberta (o frontend usa o diretório no cadastro/login),
 * mas leitura, atualização e exclusão por id só funcionam para o próprio
 * tatuador autenticado — qualquer outro id responde 404, sem revelar existência.</p>
 */
@RestController
@RequestMapping("/api/v1/tatuadores")
public class TatuadorController {

    private final TatuadorService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public TatuadorController(TatuadorService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public Page<TatuadorResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/me")
    public TatuadorResponse meuPerfil() {
        return service.buscar(tatuadorAutenticado.id(), tatuadorAutenticado.id());
    }

    @GetMapping("/{id}")
    public TatuadorResponse buscar(@PathVariable Integer id) {
        return service.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TatuadorResponse criar(@Valid @RequestBody TatuadorCreate input) {
        return service.criar(input);
    }

    @PutMapping("/{id}")
    public TatuadorResponse atualizar(@PathVariable Integer id, @Valid @RequestBody TatuadorUpdate input) {
        return service.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        service.excluir(id, tatuadorAutenticado.id());
    }

    @PostMapping("/autenticacao")
    public LoginResponse autenticar(@Valid @RequestBody LoginRequest input) {
        return service.autenticar(input);
    }
}
