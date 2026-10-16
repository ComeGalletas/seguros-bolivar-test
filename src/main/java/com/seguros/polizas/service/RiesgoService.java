package com.seguros.polizas.service;

import com.seguros.polizas.domain.Riesgo;
import com.seguros.polizas.repository.RiesgoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class RiesgoService {

    private final RiesgoRepository riesgos;
    private final CoreService core;

    public RiesgoService(RiesgoRepository riesgos, CoreService core) {
        this.riesgos = riesgos;
        this.core = core;
    }

    public Riesgo cancelar(Long id) {
        Riesgo riesgo = riesgos.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Riesgo %d no existe".formatted(id)));
        riesgo.cancelar();
        core.enviar(new CoreEvento(CoreEvento.ACTUALIZACION, riesgo.getPoliza().getId()));
        return riesgo;
    }
}
