package com.seguros.polizas.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RiesgoRequest(@NotBlank @Size(max = 255) String descripcion) {}
