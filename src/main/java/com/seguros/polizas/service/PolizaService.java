package com.seguros.polizas.service;

import com.seguros.polizas.config.PolizasProperties;
import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.Riesgo;
import com.seguros.polizas.domain.TipoPoliza;
import com.seguros.polizas.repository.PolizaRepository;
import com.seguros.polizas.repository.RiesgoRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PolizaService {

    private final PolizaRepository polizas;
    private final RiesgoRepository riesgos;
    private final CoreService core;
    private final PolizasProperties properties;

    public PolizaService(PolizaRepository polizas, RiesgoRepository riesgos, CoreService core,
                         PolizasProperties properties) {
        this.polizas = polizas;
        this.riesgos = riesgos;
        this.core = core;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Poliza> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizas.buscar(tipo, estado);
    }

    @Transactional(readOnly = true)
    public List<Riesgo> listarRiesgos(Long polizaId) {
        obtener(polizaId);
        return riesgos.findByPolizaIdOrderById(polizaId);
    }

    public Poliza renovar(Long id) {
        Poliza poliza = obtener(id);
        poliza.renovar(properties.ipc());
        core.enviar(new CoreEvento(CoreEvento.ACTUALIZACION, id));
        return poliza;
    }

    public Poliza cancelar(Long id) {
        Poliza poliza = obtener(id);
        poliza.cancelar();
        core.enviar(new CoreEvento(CoreEvento.CANCELACION, id));
        return poliza;
    }

    public Riesgo agregarRiesgo(Long polizaId, String descripcion) {
        Poliza poliza = obtener(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA");
        }
        Riesgo riesgo = riesgos.save(poliza.agregarRiesgo(descripcion));
        core.enviar(new CoreEvento(CoreEvento.ACTUALIZACION, polizaId));
        return riesgo;
    }

    private Poliza obtener(Long id) {
        return polizas.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Póliza %d no existe".formatted(id)));
    }
}
