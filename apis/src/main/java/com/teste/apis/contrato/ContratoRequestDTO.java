package com.teste.apis.contrato;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos em POST /contratos, conforme a secao 13.2 do briefing
 * (que prevalece sobre o rascunho da secao 7.1).
 */
public record ContratoRequestDTO(

        @NotBlank(message = "nome do cliente e obrigatorio")
        @Size(min = 3, max = 100, message = "nome do cliente deve ter entre 3 e 100 caracteres")
        String nomeCliente,

        @NotBlank(message = "CPF e obrigatorio")
        @CPF(message = "CPF invalido")
        String cpfCliente,

        @NotBlank(message = "telefone e obrigatorio")
        @Pattern(regexp = "^[0-9+()\\s-]{8,20}$", message = "telefone invalido")
        String telefoneCliente,

        @NotBlank(message = "nome do artista e obrigatorio")
        @Size(max = 150, message = "nome do artista deve ter no maximo 150 caracteres")
        String nomeArtista,

        @NotBlank(message = "descricao da tatuagem e obrigatoria")
        @Size(max = 1000, message = "descricao da tatuagem deve ter no maximo 1000 caracteres")
        String descricaoTatuagem,

        @NotBlank(message = "local do corpo e obrigatorio")
        @Size(max = 100, message = "local do corpo deve ter no maximo 100 caracteres")
        String localCorpo,

        @NotBlank(message = "tamanho estimado e obrigatorio")
        @Size(max = 20, message = "tamanho estimado deve ter no maximo 20 caracteres")
        String tamanhoEstimado,

        @NotNull(message = "data de inicio e obrigatoria")
        LocalDate dataInicio,

        @NotNull(message = "data de fim e obrigatoria")
        LocalDate dataFim,

        @NotNull(message = "valor orcado e obrigatorio")
        @Positive(message = "valor orcado deve ser maior que zero")
        @Digits(integer = 12, fraction = 2, message = "valor orcado deve ter no maximo duas casas decimais")
        BigDecimal valorOrcado,

        @NotNull(message = "informe se o sinal foi pago")
        Boolean sinalPago,

        @NotBlank(message = "forma de pagamento e obrigatoria")
        @Size(max = 60, message = "forma de pagamento deve ter no maximo 60 caracteres")
        String formaPagamento

) {

    @AssertTrue(message = "dataFim nao pode ser anterior a dataInicio")
    public boolean isPeriodoValido() {
        if (dataInicio == null || dataFim == null) {
            return true;
        }
        return !dataFim.isBefore(dataInicio);
    }
}
