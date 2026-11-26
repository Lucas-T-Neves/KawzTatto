package com.kawz.tattoo.api.anamnese.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class FlexibleLocalDateDeserializer extends StdDeserializer<LocalDate> {
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FlexibleLocalDateDeserializer() { super(LocalDate.class); }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(value, BR);
            } catch (DateTimeParseException invalid) {
                return (LocalDate) context.handleWeirdStringValue(LocalDate.class, value,
                        "Use yyyy-MM-dd ou dd/MM/yyyy");
            }
        }
    }
}
