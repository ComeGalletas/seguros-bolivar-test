package com.seguros.polizas.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Evento que se notifica al CORE externo. */
public record CoreEvento(@NotBlank String evento, @NotNull Long polizaId) {

    public static final String ACTUALIZACION = "ACTUALIZACION";
    public static final String CANCELACION = "CANCELACION";
}
