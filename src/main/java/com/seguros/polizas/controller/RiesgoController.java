package com.seguros.polizas.controller;

import com.seguros.polizas.controller.dto.RiesgoResponse;
import com.seguros.polizas.service.RiesgoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
public class RiesgoController {

    private final RiesgoService service;

    public RiesgoController(RiesgoService service) {
        this.service = service;
    }

    @PostMapping("/{id}/cancelar")
    public RiesgoResponse cancelar(@PathVariable Long id) {
        return RiesgoResponse.from(service.cancelar(id));
    }
}
