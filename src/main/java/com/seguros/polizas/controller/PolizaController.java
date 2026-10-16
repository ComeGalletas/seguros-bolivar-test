package com.seguros.polizas.controller;

import com.seguros.polizas.controller.dto.PolizaResponse;
import com.seguros.polizas.controller.dto.RiesgoRequest;
import com.seguros.polizas.controller.dto.RiesgoResponse;
import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.TipoPoliza;
import com.seguros.polizas.service.PolizaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador principal para la gestión de pólizas.
 * Proporciona endpoints para listar, renovar, cancelar pólizas y gestionar riesgos asociados.
 **/
@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final PolizaService service;

    public PolizaController(PolizaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PolizaResponse> listar(@RequestParam(required = false) TipoPoliza tipo,
                                       @RequestParam(required = false) EstadoPoliza estado) {
        return service.listar(tipo, estado).stream().map(PolizaResponse::from).toList();
    }

    @GetMapping("/{id}/riesgos")
    public List<RiesgoResponse> riesgos(@PathVariable Long id) {
        return service.listarRiesgos(id).stream().map(RiesgoResponse::from).toList();
    }

    @PostMapping("/{id}/renovar")
    public PolizaResponse renovar(@PathVariable Long id) {
        return PolizaResponse.from(service.renovar(id));
    }

    @PostMapping("/{id}/cancelar")
    public PolizaResponse cancelar(@PathVariable Long id) {
        return PolizaResponse.from(service.cancelar(id));
    }

    @PostMapping("/{id}/riesgos")
    @ResponseStatus(HttpStatus.CREATED)
    public RiesgoResponse agregarRiesgo(@PathVariable Long id, @Valid @RequestBody RiesgoRequest request) {
        return RiesgoResponse.from(service.agregarRiesgo(id, request.descripcion()));
    }
}
