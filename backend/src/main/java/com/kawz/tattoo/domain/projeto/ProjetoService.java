package com.kawz.tattoo.domain.projeto;

import com.kawz.tattoo.api.projeto.ProjetoMapper;
import com.kawz.tattoo.api.projeto.dto.ProjetoInput;
import com.kawz.tattoo.api.projeto.dto.ProjetoResponse;
import com.kawz.tattoo.api.projeto.dto.StatusRequest;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.BusinessException;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.imagem.Imagem;
import com.kawz.tattoo.domain.imagem.ImagemRepository;
import com.kawz.tattoo.domain.movimentacao.MovimentacaoFinanceiraRepository;
import com.kawz.tattoo.domain.tatuador.Tatuador;
import com.kawz.tattoo.domain.tatuador.TatuadorRepository;
import com.kawz.tattoo.application.gateway.ArmazenamentoImagensGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Projetos do tatuador autenticado. O proprietário vem sempre do token: o
 * {@code tatuadorId} enviado no JSON é ignorado (mantido apenas por
 * compatibilidade com o contrato do frontend).
 */
@Service
public class ProjetoService {

    private static final Resumo SEM_MOVIMENTACOES = new Resumo(BigDecimal.ZERO, false);

    private final ProjetoRepository projetos;
    private final ClienteRepository clientes;
    private final TatuadorRepository tatuadores;
    private final MovimentacaoFinanceiraRepository movimentacoes;
    private final ImagemRepository imagens;
    private final ArmazenamentoImagensGateway armazenamento;

    public ProjetoService(ProjetoRepository projetos, ClienteRepository clientes, TatuadorRepository tatuadores,
                          MovimentacaoFinanceiraRepository movimentacoes, ImagemRepository imagens,
                          ArmazenamentoImagensGateway armazenamento) {
        this.projetos = projetos;
        this.clientes = clientes;
        this.tatuadores = tatuadores;
        this.movimentacoes = movimentacoes;
        this.imagens = imagens;
        this.armazenamento = armazenamento;
    }

    /** Recebido e sinal pago de um projeto (base do saldo). */
    public record Resumo(BigDecimal recebido, boolean sinalPago) {}

    @Transactional(readOnly = true)
    public Page<ProjetoResponse> listar(Integer tatuadorId, Pageable pageable) {
        Map<Integer, Resumo> resumos = new HashMap<>();
        for (Object[] linha : movimentacoes.resumirPorProjeto(tatuadorId)) {
            resumos.put((Integer) linha[0], new Resumo((BigDecimal) linha[1],
                    ((Number) linha[2]).intValue() == 1));
        }
        // Page.map simplifica a conversão de cada Projeto e permite calcular
        // o resumo antes de gerar o DTO, mantendo os metadados da página.
        return projetos.findByTatuadorId(tatuadorId, pageable).map(projeto -> {
            Resumo resumo = resumos.getOrDefault(projeto.getId(), SEM_MOVIMENTACOES);
            return ProjetoMapper.toResponse(projeto, resumo.recebido(), resumo.sinalPago(),
                    referencia(projeto.getId(), tatuadorId));
        });
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscar(Integer id, Integer tatuadorId) {
        Projeto projeto = obterDoTatuador(id, tatuadorId);
        Resumo resumo = resumo(id);
        return ProjetoMapper.toResponse(projeto, resumo.recebido(), resumo.sinalPago(),
                referencia(id, tatuadorId));
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponse> listarPorCliente(Integer clienteId, Integer tatuadorId) {
        obterClienteDoTatuador(clienteId, tatuadorId);
         return projetos.findByClienteIdAndTatuadorIdOrderByIdDesc(clienteId, tatuadorId).stream()
                .map(projeto -> {
                    Resumo resumo = resumo(projeto.getId());
                    return ProjetoMapper.toResponse(projeto, resumo.recebido(), resumo.sinalPago(),
                            referencia(projeto.getId(), tatuadorId));
                }).toList();
    }

    @Transactional
    public ProjetoResponse criar(ProjetoInput input, Integer tatuadorId) {
        validarValores(input);
        Projeto projeto = new Projeto(obterTatuador(tatuadorId),
                obterClienteDoTatuador(input.clienteId(), tatuadorId),
                nomeProjeto(input), input.descricao(), input.linkPasta(), input.statusProjeto(),
                input.valorTotal(), input.valorSinal(), input.quantidadeSessoes(), input.dataValidade(),
                input.lembretePre(), input.lembretePost());
        aplicarCaracteristicas(projeto, input);
        // Projeto novo: nenhuma movimentação possível ainda
        Projeto salvo = projetos.save(projeto);
        return ProjetoMapper.toResponse(salvo, BigDecimal.ZERO, false, referencia(salvo.getId(), tatuadorId));
    }

    @Transactional
    public ProjetoResponse atualizar(Integer id, ProjetoInput input, Integer tatuadorId) {
        validarValores(input);
        Projeto projeto = obterDoTatuador(id, tatuadorId);
        projeto.vincular(obterTatuador(tatuadorId), obterClienteDoTatuador(input.clienteId(), tatuadorId));
        projeto.atualizar(nomeProjeto(input), input.descricao(), input.linkPasta(), input.statusProjeto(),
                input.valorTotal(), input.valorSinal(), input.quantidadeSessoes(), input.dataValidade(),
                input.lembretePre(), input.lembretePost());
        aplicarCaracteristicas(projeto, input);
        Resumo resumo = resumo(id);
        return ProjetoMapper.toResponse(projeto, resumo.recebido(), resumo.sinalPago(),
                referencia(id, tatuadorId));
    }

    @Transactional
    public ProjetoResponse alterarStatus(Integer id, StatusRequest input, Integer tatuadorId) {
        Projeto projeto = obterDoTatuador(id, tatuadorId);
        projeto.alterarStatus(input.status());
        Resumo resumo = resumo(id);
        return ProjetoMapper.toResponse(projeto, resumo.recebido(), resumo.sinalPago(),
                referencia(id, tatuadorId));
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        Projeto projeto = obterDoTatuador(id, tatuadorId);
        // as imagens saem do banco em cascata (ON DELETE CASCADE); aqui limpamos também o armazenamento
        List<String> publicIds = imagens.listarPublicIds(id, tatuadorId);
        projetos.delete(projeto);
        projetos.flush(); // se houver sessões/movimentações, a violação de FK estoura aqui, antes de tocar no storage
        publicIds.forEach(armazenamento::remover);
    }

    /** Carrega o projeto garantindo que pertence ao tatuador autenticado. */
    @Transactional(readOnly = true)
    public Projeto obterDoTatuador(Integer id, Integer tatuadorId) {
        return projetos.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    /** Recebido e sinal pago calculados a partir das movimentações. */
    @Transactional(readOnly = true)
    public Resumo resumo(Integer projetoId) {
        BigDecimal recebido = movimentacoes.somarRecebido(projetoId);
        boolean sinalPago = movimentacoes.existsByProjetoIdAndCategoriaIgnoreCaseAndStatusPagamentoIgnoreCase(
                projetoId, "sinal", "pago");
        return new Resumo(recebido == null ? BigDecimal.ZERO : recebido, sinalPago);
    }

    private void validarValores(ProjetoInput input) {
        if (input.valorSinal().compareTo(input.valorTotal()) > 0) {
            throw new BusinessException("O sinal não pode ser maior que o valor total");
        }
    }

    private void aplicarCaracteristicas(Projeto projeto, ProjetoInput input) {
        projeto.getCaracteristicas().atualizarCaracteristicas(input.ideiaExpectativa(), input.estiloArte(),
                input.localizacaoCorpo(), input.tamanho(), input.dificuldade(), input.cores(), input.complexidade());
    }

    private String nomeProjeto(ProjetoInput input) {
        return input.nome() == null || input.nome().isBlank() ? input.descricao() : input.nome().trim();
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Tatuador obterTatuador(Integer id) {
        return tatuadores.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tatuador", id));
    }

    private Imagem referencia(Integer projetoId, Integer tatuadorId) {
        return imagens.findFirstByProjetoIdAndProjeto_Tatuador_IdOrderByCriadoEmDesc(projetoId, tatuadorId)
                .orElse(null);
    }
}
