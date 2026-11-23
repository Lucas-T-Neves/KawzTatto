package com.kawz.tattoo.api.anamnese;

import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.domain.anamnese.Anamnese;
import com.kawz.tattoo.domain.cliente.Cliente;


public final class AnamneseMapper {
    private AnamneseMapper() {}

    public static AnamneseResponse toResponse(Anamnese value) {
        return new AnamneseResponse(value.getId(), value.getCliente().getId(), value.getDadosSaude(),
                value.getPreparacaoTatuagem());
    }

    public static Anamnese toEntity(AnamneseInput value, Cliente cliente) {
        Anamnese r = new Anamnese();
        r.setCliente(cliente);
        r.setDadosSaude(value.dadosSaude());
        r.setPreparacaoTatuagem(value.preparacaoTatuagem());

        return r;
    }


}
