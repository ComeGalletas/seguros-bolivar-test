package com.seguros.polizas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Integración simulada con el CORE: solo deja constancia en logs del intento de envío. */
@Service
public class CoreService {

    private static final Logger log = LoggerFactory.getLogger(CoreService.class);

    public void enviar(CoreEvento evento) {
        log.info("[CORE-MOCK] Intento de envío al CORE: evento={}, polizaId={}", evento.evento(), evento.polizaId());
    }
}
