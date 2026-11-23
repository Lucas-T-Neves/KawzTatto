package com.kawz.tattoo.api.caracteristicasprojeto;

import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoInput;
import com.kawz.tattoo.api.caracteristicasprojeto.dto.CaracteristicasProjetoResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.caracteristicasprojeto.CaracteristicasProjetoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/caracteristicas")
public class CaracteristicasProjetoController {

    private final CaracteristicasProjetoService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public CaracteristicasProjetoController(CaracteristicasProjetoService service,
                                            TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public CaracteristicasProjetoResponse buscar(@PathVariable Integer projetoId) {
        return service.buscar(projetoId, tatuadorAutenticado.id());
    }

    @PutMapping
    public CaracteristicasProjetoResponse atualizar(@PathVariable Integer projetoId,
                                                    @Valid @RequestBody CaracteristicasProjetoInput input) {
        return service.atualizar(projetoId, input, tatuadorAutenticado.id());
    }
}
