package com.kawz.tattoo.api.anamnese.dto;

import java.time.LocalDate;
import java.time.Instant;

public record AnamneseResponse(
        Integer id,
        Integer clienteId,
        String logoUrl,
        String nome,
        LocalDate dataNascimento,
        String ocupacao,
        String telefone,
        Boolean whatsapp,
        String endereco,
        String rg,
        String cpf,
        AnamneseInput.SaudeInput saude,
        LocalDate dataAssinatura,
        String assinaturaUrl,
        AnamneseInput.ProcedimentoInput procedimento,
        String pdfUrl,
        String transactionRef,
        String documentoAssinadoUrl,
        String documentoAssinadoPublicId,
        String documentoAssinadoNome,
        String documentoAssinadoMime,
        Long documentoAssinadoBytes,
        Instant documentoAssinadoEm,
        String dadosSaude,
        String preparacaoTatuagem) {}
