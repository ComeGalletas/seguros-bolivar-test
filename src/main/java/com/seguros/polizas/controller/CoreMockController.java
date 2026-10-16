package com.seguros.polizas.controller;

import com.seguros.polizas.service.CoreEvento;
import com.seguros.polizas.service.CoreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Mock del sistema CORE externo: registra en logs el evento recibido. */
@RestController
public class CoreMockController {

    private final CoreService core;

    public CoreMockController(CoreService core) {
        this.core = core;
    }

    @PostMapping("/core-mock/evento")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void registrar(@Valid @RequestBody CoreEvento evento) {
        core.enviar(evento);
    }
}
