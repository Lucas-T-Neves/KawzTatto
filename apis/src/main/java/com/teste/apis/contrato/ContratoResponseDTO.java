package com.teste.apis.contrato;

import java.time.OffsetDateTime;

/** Resposta devolvida pela API. Nenhuma entity aparece aqui. */
public record ContratoResponseDTO(
        Long id,
        String nomeCliente,
        StatusContrato status,
        String pdfUrl,
        OffsetDateTime criadoEm
) {

    public static ContratoResponseDTO de(Contrato contrato) {
        return new ContratoResponseDTO(
                contrato.getId(),
                contrato.getNomeCliente(),
                contrato.getStatus(),
                contrato.getPdfUrl(),
                contrato.getCriadoEm());
    }
}
