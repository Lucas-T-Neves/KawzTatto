package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.DashboardResponse;
import com.kawz.tattoo.domain.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) { this.service = service; }

    @GetMapping
    public DashboardResponse buscar(@RequestParam(required = false) String mes) {
        YearMonth periodo = mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes);
        return service.resumir(periodo);
    }
}
