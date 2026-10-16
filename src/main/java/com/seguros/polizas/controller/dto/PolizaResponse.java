package com.seguros.polizas.controller.dto;

import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.TipoPoliza;
import java.math.BigDecimal;

public record PolizaResponse(Long id, TipoPoliza tipo, EstadoPoliza estado, BigDecimal canon, BigDecimal prima) {

    public static PolizaResponse from(Poliza p) {
        return new PolizaResponse(p.getId(), p.getTipo(), p.getEstado(), p.getCanon(), p.getPrima());
    }
}
