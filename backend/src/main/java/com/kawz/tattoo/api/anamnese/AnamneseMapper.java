package com.kawz.tattoo.api.anamnese;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.domain.anamnese.Anamnese;
import com.kawz.tattoo.domain.cliente.Cliente;

public final class AnamneseMapper {
    private AnamneseMapper() {}

    public static AnamneseResponse toResponse(Anamnese value, ObjectMapper json) {
        return new AnamneseResponse(value.getId(), value.getCliente().getId(), value.getLogoUrl(),
                value.getNome(), value.getDataNascimento(), value.getOcupacao(), value.getTelefone(),
                value.getWhatsapp(), value.getEndereco(), value.getRg(), value.getCpf(),
                read(json, value.getSaudeJson(), AnamneseInput.SaudeInput.class),
                value.getDataAssinatura(), value.getAssinaturaUrl(),
                read(json, value.getProcedimentoJson(), AnamneseInput.ProcedimentoInput.class),
                value.getPdfUrl(), value.getTransactionRef(),
                value.getDocumentoAssinadoUrl(), value.getDocumentoAssinadoPublicId(),
                value.getDocumentoAssinadoNome(), value.getDocumentoAssinadoMime(),
                value.getDocumentoAssinadoBytes(), value.getDocumentoAssinadoEm(),
                value.getDadosSaude(), value.getPreparacaoTatuagem());
    }

    public static Anamnese toEntity(AnamneseInput input, Cliente cliente, ObjectMapper json) {
        Anamnese value = new Anamnese(cliente, input.dadosSaude(), input.preparacaoTatuagem());
        preencher(value, input, cliente, json);
        return value;
    }

    public static void preencher(Anamnese value, AnamneseInput input, Cliente cliente, ObjectMapper json) {
        value.preencher(cliente, input.logoUrl(), input.nome(), input.dataNascimento(), input.ocupacao(),
                input.telefone(), input.whatsapp(), input.endereco(), input.rg(), input.cpf(),
                write(json, input.saude()), input.dataAssinatura(), input.assinaturaUrl(),
                write(json, input.procedimento()), input.dadosSaude(), input.preparacaoTatuagem());
    }

    private static <T> T read(ObjectMapper json, String value, Class<T> type) {
        if (value == null || value.isBlank()) return null;
        try {
            return json.readValue(value, type);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private static String write(ObjectMapper json, Object value) {
        if (value == null) return null;
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Não foi possível serializar os dados da anamnese", ex);
        }
    }
}
