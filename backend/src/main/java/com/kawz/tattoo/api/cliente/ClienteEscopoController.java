package com.kawz.tattoo.api.cliente;

import com.kawz.tattoo.api.cliente.dto.ConversaResponse;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.sessao.dto.SessaoResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.cliente.ClienteService;
import com.kawz.tattoo.domain.projeto.ProjetoService;
import com.kawz.tattoo.domain.sessao.SessaoService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}")
public class ClienteEscopoController {

    private final ClienteService clientes;
    private final ProjetoService projetos;
    private final SessaoService sessoes;
    private final TatuadorAutenticado tatuadorAutenticado;

    public ClienteEscopoController(ClienteService clientes, ProjetoService projetos,
                                   SessaoService sessoes, TatuadorAutenticado tatuadorAutenticado) {
        this.clientes = clientes;
        this.projetos = projetos;
        this.sessoes = sessoes;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping("/projetos")
    public List<ProjetoResponse> listarProjetos(@PathVariable Integer clienteId) {
        return projetos.listarPorCliente(clienteId, tatuadorAutenticado.id());
    }

    @GetMapping("/sessoes")
    public Page<SessaoResponse> listarSessoes(@PathVariable Integer clienteId,
                                              @RequestParam(defaultValue = "0") int pagina,
                                              @RequestParam(defaultValue = "20") int tamanho) {
        return sessoes.listarPorCliente(clienteId, tatuadorAutenticado.id(), Paginacao.de(pagina, tamanho));
    }

    @GetMapping("/conversa")
    public ConversaResponse conversa(@PathVariable Integer clienteId) {
        return clientes.conversa(clienteId, tatuadorAutenticado.id());
    }
}
