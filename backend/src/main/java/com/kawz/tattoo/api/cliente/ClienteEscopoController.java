package com.kawz.tattoo.api.cliente;

import com.kawz.tattoo.api.cliente.dto.ClienteResponse;
import com.kawz.tattoo.api.cliente.dto.ConversaResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.cliente.ClienteService;
import com.kawz.tattoo.domain.projeto.ProjetoService;
import com.kawz.tattoo.domain.sessao.SessaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}")
public class ClienteEscopoController {
    private final ClienteService clientes;
    private final ProjetoService projetos;
    private final SessaoService sessoes;

    public ClienteEscopoController(ClienteService clientes, ProjetoService projetos, SessaoService sessoes) {
        this.clientes = clientes;
        this.projetos = projetos;
        this.sessoes = sessoes;
    }

    @GetMapping("/projetos")
    public java.util.List<ProjetoResponse> listarProjetos(@PathVariable Integer clienteId) {
        return projetos.listarPorCliente(clienteId);
    }

    @GetMapping("/sessoes")
    public Page<SessaoResponse> listarSessoes(@PathVariable Integer clienteId,
                                               @RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "20") int tamanho) {
        return sessoes.listarPorCliente(clienteId, Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/conversa")
    public ConversaResponse conversa(@PathVariable Integer clienteId) {
        return clientes.conversa(clienteId);
    }
}
