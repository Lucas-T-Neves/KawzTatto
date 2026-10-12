package com.teste.apis.contrato;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.teste.apis.exception.IntegrationException;
import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.integration.apitemplate.ApiTemplateClient;
import com.teste.apis.integration.apitemplate.dto.ApiTemplatePdfResponse;

/**
 * Fluxo do contrato: salva PENDENTE, chama o provedor e marca GERADO ou FALHA.
 * Nao ha @Transactional: a transacao de banco fica curta e nenhuma transacao
 * fica aberta durante a chamada HTTP externa.
 */
@Service
public class ContratoService {

    private static final Logger log = LoggerFactory.getLogger(ContratoService.class);

    private final ContratoRepository repository;
    private final ApiTemplateClient apiTemplateClient;

    public ContratoService(ContratoRepository repository, ApiTemplateClient apiTemplateClient) {
        this.repository = repository;
        this.apiTemplateClient = apiTemplateClient;
    }

    /** Salva o contrato, gera o PDF e devolve os dados com o link. */
    public ContratoResponseDTO criar(ContratoRequestDTO request) {
        Contrato contrato = repository.save(Contrato.novo(request, OffsetDateTime.now()));

        try {
            ApiTemplatePdfResponse resposta = apiTemplateClient.gerarPdf(request);
            if (!resposta.sucesso()) {
                log.warn("Provedor respondeu sem download_url para o contrato id={}", contrato.getId());
                throw IntegrationException.falhaProvedor("APITemplate.io",
                        "Provedor nao retornou o link do PDF", null);
            }
            contrato.marcarGerado(resposta.downloadUrl(), resposta.transactionRef());
            return ContratoResponseDTO.de(repository.save(contrato));
        } catch (IntegrationException e) {
            contrato.marcarFalha();
            repository.save(contrato);
            throw e;
        }
    }

    /** Busca um contrato por id ou devolve 404. */
    public ContratoResponseDTO buscarPorId(Long id) {
        return ContratoResponseDTO.de(buscarEntidade(id));
    }

    /** Lista todos os contratos, do mais recente para o mais antigo. */
    public List<ContratoResponseDTO> listar() {
        return repository.findAllByOrderByCriadoEmDesc().stream()
                .map(ContratoResponseDTO::de)
                .toList();
    }

    Contrato buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato nao encontrado: " + id));
    }
}
