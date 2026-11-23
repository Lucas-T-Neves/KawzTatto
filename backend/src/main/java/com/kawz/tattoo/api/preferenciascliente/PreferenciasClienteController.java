package com.kawz.tattoo.api.preferenciascliente;

import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteInput;
import com.kawz.tattoo.api.preferenciascliente.dto.PreferenciasClienteResponse;
import com.kawz.tattoo.application.security.TatuadorAutenticado;
import com.kawz.tattoo.domain.preferenciascliente.PreferenciasClienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes/{clienteId}/preferencias")
public class PreferenciasClienteController {

    private final PreferenciasClienteService service;
    private final TatuadorAutenticado tatuadorAutenticado;

    public PreferenciasClienteController(PreferenciasClienteService service,
                                         TatuadorAutenticado tatuadorAutenticado) {
        this.service = service;
        this.tatuadorAutenticado = tatuadorAutenticado;
    }

    @GetMapping
    public PreferenciasClienteResponse buscar(@PathVariable Integer clienteId) {
        return service.buscar(clienteId, tatuadorAutenticado.id());
    }

    @PutMapping
    public PreferenciasClienteResponse atualizar(@PathVariable Integer clienteId,
                                                 @Valid @RequestBody PreferenciasClienteInput input) {
        return service.atualizar(clienteId, input, tatuadorAutenticado.id());
    }
}
