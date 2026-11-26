package com.kawz.tattoo.api.anamnese.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AnamneseInput(
        @NotNull Integer clienteId,
        @JsonAlias("logo_url") @Size(max = 1000) String logoUrl,
        @Size(max = 150) String nome,
        @JsonAlias("data_nascimento") @JsonDeserialize(using = FlexibleLocalDateDeserializer.class) LocalDate dataNascimento,
        @Size(max = 120) String ocupacao,
        @Size(max = 20) String telefone,
        Boolean whatsapp,
        @Size(max = 500) String endereco,
        @Size(max = 30) String rg,
        @Size(max = 20) String cpf,
        @Valid SaudeInput saude,
        @JsonAlias("data_assinatura") @JsonDeserialize(using = FlexibleLocalDateDeserializer.class) LocalDate dataAssinatura,
        @JsonAlias("assinatura_url") @Size(max = 1000) String assinaturaUrl,
        @Valid ProcedimentoInput procedimento,
        @Size(max = 45) String dadosSaude,
        @Size(max = 45) String preparacaoTatuagem) {

    public record SaudeInput(
            @JsonAlias("tratamento_medico") Boolean tratamentoMedico,
            @JsonAlias("tratamento_medico_especifique") @Size(max = 2000) String tratamentoMedicoEspecifique,
            @JsonAlias("cirurgia_recente") Boolean cirurgiaRecente,
            @JsonAlias("cirurgia_recente_especifique") @Size(max = 2000) String cirurgiaRecenteEspecifique,
            Boolean alergia,
            @JsonAlias("alergia_especifique") @Size(max = 2000) String alergiaEspecifique,
            Boolean gestante,
            Boolean amamentando,
            Boolean marcapasso,
            @JsonAlias("disturbio_circulatorio") Boolean disturbioCirculatorio,
            Boolean cancer,
            @JsonAlias("uso_drogas") Boolean usoDrogas,
            @JsonAlias("alimentou_24h") Boolean alimentou24h,
            Boolean queloide,
            Boolean diabetes,
            @JsonAlias("diabetes_tipo") @Size(max = 60) String diabetesTipo,
            @JsonAlias("diabetes_controlada") Boolean diabetesControlada,
            Boolean convulsoes,
            @JsonAlias("convulsoes_especifique") @Size(max = 2000) String convulsoesEspecifique,
            @JsonAlias("infectocontagiosas") Boolean infectocontagiosas,
            @JsonAlias("infectocontagiosas_especifique") @Size(max = 2000) String infectocontagiosasEspecifique,
            Boolean vitiligo,
            Boolean hipertensao,
            Boolean anemia,
            Boolean cardiopatia,
            Boolean hemofilia,
            @JsonAlias("problema_pele") Boolean problemaPele,
            Boolean hepatite,
            @JsonAlias("dormiu_ultima_noite") Boolean dormiuUltimaNoite,
            @JsonAlias("outro_problema") @Size(max = 2000) String outroProblema) {}

    public record ProcedimentoInput(
            String local,
            @Size(max = 4000) String observacoes,
            @Size(max = 150) String profissional,
            @JsonDeserialize(using = FlexibleLocalDateDeserializer.class) LocalDate data) {}
}
