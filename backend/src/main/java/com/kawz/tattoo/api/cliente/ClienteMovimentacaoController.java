package com.kawz.tattoo.api.cliente;

import com.kawz.tattoo.api.movimentacao.dto.MovimentacaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.cliente.ClienteService;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}/movimentacoes")
public class ClienteMovimentacaoController {

    private final MovimentacaoService service;
    private final ClienteService clientes;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ClienteMovimentacaoController(MovimentacaoService service, ClienteService clientes,
                                         TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.clientes = clientes;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public Page<MovimentacaoResponse> listar(@PathVariable Integer clienteId,
                                             @RequestParam(defaultValue = "0") int pagina,
                                             @RequestParam(defaultValue = "20") int tamanho) {
        Integer tatuadorId = tatuadorAutenticado.id();
        // valida a posse do cliente antes de listar (404 se for de outro tatuador)
        clientes.obterDoTatuador(clienteId, tatuadorId);
        return service.listar(tatuadorId, null, null, null, clienteId,
                Paginacao.de(pagina, tamanho));
    }
}
