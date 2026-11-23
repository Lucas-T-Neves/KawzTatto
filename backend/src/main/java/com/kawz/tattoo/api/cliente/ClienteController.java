package com.kawz.tattoo.api.cliente;

import com.kawz.tattoo.api.cliente.dto.ClienteInput;
import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.cliente.ClienteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ClienteController(ClienteService service, TatuadorAutenticado tatuadorAutenticado) {
        this.clienteService = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    /** Lista apenas os clientes do tatuador autenticado (contrato: content + totalPages). */
    @GetMapping
    public Page<ClienteResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return clienteService.listar(tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Integer id) {
        return clienteService.buscar(id, tatuadorAutenticado.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse criar(@Valid @RequestBody ClienteInput input) {
        return clienteService.criar(input, tatuadorAutenticado.id());
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ClienteInput input) {
        return clienteService.atualizar(id, input, tatuadorAutenticado.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        clienteService.excluir(id, tatuadorAutenticado.id());
    }
}
