package com.teste.apis.integration.nylas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Formato de `when` da API do Nylas (timestamps Unix em segundos). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NylasWhenDTO(

        @JsonProperty("start_time")
        Long startTime,

        @JsonProperty("end_time")
        Long endTime,

        @JsonProperty("start_timezone")
        String startTimezone,

        @JsonProperty("end_timezone")
        String endTimezone
) {
}
