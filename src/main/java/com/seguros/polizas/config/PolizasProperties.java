package com.seguros.polizas.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parámetros de negocio y seguridad configurables en application.yml (prefijo "polizas"). */
@ConfigurationProperties("polizas")
public record PolizasProperties(String apiKey, BigDecimal ipc) {}
