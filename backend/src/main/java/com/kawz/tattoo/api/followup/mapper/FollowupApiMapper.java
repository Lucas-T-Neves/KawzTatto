package com.kawz.tattoo.api.followup.mapper;

import com.kawz.tattoo.api.followup.dto.FollowUpResponse;
import com.kawz.tattoo.domain.followup.FollowUp;


public final class FollowupApiMapper {
    private FollowupApiMapper() {}

    public static FollowUpResponse toResponse(FollowUp value) {
        return new FollowUpResponse(value.getId(), value.getCliente().getId(), value.getTipoContato(),
                value.getStatusContato(), value.getDataPrevista(), value.getDataRealizacao(),
                value.getDescricaoObservacao(), value.getProximaAcao());
    }
}
