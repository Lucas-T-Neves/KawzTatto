package com.teste.apis.contrato;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Dados do estudio, injetados no PDF do contrato. */
@Getter
@Setter
@ConfigurationProperties(prefix = "estudio")
public class EstudioProperties {

    @NotBlank
    private String nome;

    @NotBlank
    private String documento;
}
