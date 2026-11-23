package com.kawz.tattoo.api.dashboard;

import com.kawz.tattoo.api.dashboard.dto.DashboardResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.dashboard.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public DashboardController(DashboardService service, TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public DashboardResponse buscar(@RequestParam(required = false) String mes) {
        YearMonth periodo = mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes);
        return service.resumir(periodo, tatuadorAutenticado.id());
    }
}
