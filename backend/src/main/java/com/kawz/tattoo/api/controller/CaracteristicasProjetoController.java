package com.kawz.tattoo.api.controller;

import com.kawz.tattoo.api.dto.ApiModels.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.dto.ApiModels.CaracteristicasProjetoResponse;
import com.kawz.tattoo.domain.service.CaracteristicasProjetoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/caracteristicas")
public class CaracteristicasProjetoController {
    private final CaracteristicasProjetoService service;

    public CaracteristicasProjetoController(CaracteristicasProjetoService service) { this.service = service; }

    @GetMapping
    public CaracteristicasProjetoResponse buscar(@PathVariable Integer projetoId) {
        return service.buscar(projetoId);
    }

    @PutMapping
    public CaracteristicasProjetoResponse atualizar(@PathVariable Integer projetoId,
                                                    @Valid @RequestBody CaracteristicasProjetoInput input) {
        return service.atualizar(projetoId, input);
    }
}
