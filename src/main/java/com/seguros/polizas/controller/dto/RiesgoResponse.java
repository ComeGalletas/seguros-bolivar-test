package com.seguros.polizas.controller.dto;

import com.seguros.polizas.domain.EstadoRiesgo;
import com.seguros.polizas.domain.Riesgo;

public record RiesgoResponse(Long id, String descripcion, EstadoRiesgo estado, Long polizaId) {

    public static RiesgoResponse from(Riesgo r) {
        return new RiesgoResponse(r.getId(), r.getDescripcion(), r.getEstado(), r.getPoliza().getId());
    }
}
