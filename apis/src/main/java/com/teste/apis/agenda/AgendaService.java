package com.teste.apis.agenda;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.teste.apis.agenda.dto.EventoRequestDTO;
import com.teste.apis.agenda.dto.EventoResponseDTO;
import com.teste.apis.exception.IntegrationException;
import com.teste.apis.integration.nylas.NylasClient;
import com.teste.apis.integration.nylas.dto.NylasEventoRequestDTO;
import com.teste.apis.integration.nylas.dto.NylasEventoResponseDTO;
import com.teste.apis.integration.nylas.dto.NylasParticipanteDTO;
import com.teste.apis.integration.nylas.dto.NylasWhenDTO;

/**
 * Fluxo da agenda. O Nylas e a fonte da verdade: nao ha tabela propria,
 * apenas traducao entre o formato do dominio e o do provedor.
 */
@Service
public class AgendaService {

    private static final String FUSO_PADRAO = "America/Sao_Paulo";

    private final NylasClient nylasClient;

    public AgendaService(NylasClient nylasClient) {
        this.nylasClient = nylasClient;
    }

    /** Lista eventos no intervalo (inicio e fim em ISO 8601, ambos opcionais). */
    public List<EventoResponseDTO> listar(OffsetDateTime inicio, OffsetDateTime fim) {
        if (inicio != null && fim != null && !fim.isAfter(inicio)) {
            throw new com.teste.apis.exception.RegraNegocioException("fim deve ser depois de inicio");
        }
        return nylasClient.listar(paraTimestamp(inicio), paraTimestamp(fim)).stream()
                .map(AgendaService::paraDominio)
                .toList();
    }

    /** Cria um evento. */
    public EventoResponseDTO criar(EventoRequestDTO request) {
        String fuso = fusoOuPadrao(request.fusoHorario());
        NylasEventoResponseDTO criado = nylasClient.criar(nylasClient.montarCorpo(request, fuso));
        if (criado == null) {
            throw IntegrationException.falhaProvedor("Nylas",
                    "O provedor nao devolveu o evento criado", null);
        }
        return paraDominio(criado);
    }

    /** Atualiza um evento. */
    public EventoResponseDTO atualizar(String eventoId, EventoRequestDTO request) {
        String fuso = fusoOuPadrao(request.fusoHorario());
        NylasEventoResponseDTO atualizado = nylasClient.atualizar(eventoId, nylasClient.montarCorpo(request, fuso));
        if (atualizado == null) {
            throw IntegrationException.falhaProvedor("Nylas",
                    "O provedor nao devolveu o evento atualizado: " + eventoId, null);
        }
        return paraDominio(atualizado);
    }

    /** Apaga um evento. */
    public void remover(String eventoId) {
        nylasClient.remover(eventoId);
    }

    private Long paraTimestamp(OffsetDateTime dataHora) {
        return dataHora == null ? null : dataHora.toEpochSecond();
    }

    private String fusoOuPadrao(String fuso) {
        if (fuso == null || fuso.isBlank()) {
            return FUSO_PADRAO;
        }
        try {
            ZoneId.of(fuso);
            return fuso;
        } catch (java.time.DateTimeException e) {
            throw new com.teste.apis.exception.RegraNegocioException("Fuso horario invalido: " + fuso);
        }
    }

    private static EventoResponseDTO paraDominio(NylasEventoResponseDTO evento) {
        NylasWhenDTO when = evento.when();
        String fuso = when == null ? null : when.startTimezone();
        ZoneId zona = fuso == null ? ZoneId.of(FUSO_PADRAO) : ZoneId.of(fuso);

        OffsetDateTime inicio = quando(when == null ? null : when.startTime(), zona);
        OffsetDateTime fim = quando(when == null ? null : when.endTime(), zona);

        List<EventoResponseDTO.ParticipanteResponseDTO> participantes = new ArrayList<>();
        if (evento.participantes() != null) {
            for (NylasParticipanteDTO p : evento.participantes()) {
                participantes.add(new EventoResponseDTO.ParticipanteResponseDTO(p.nome(), p.email()));
            }
        }

        return new EventoResponseDTO(
                evento.id(),
                evento.titulo(),
                inicio,
                fim,
                fuso,
                evento.descricao(),
                evento.local(),
                participantes);
    }

    private static OffsetDateTime quando(Long epochSecond, ZoneId zona) {
        return epochSecond == null ? null : OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), zona);
    }
}
