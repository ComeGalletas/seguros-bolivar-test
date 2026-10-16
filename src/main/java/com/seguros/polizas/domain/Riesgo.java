package com.seguros.polizas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    protected Riesgo() {}

    Riesgo(String descripcion, Poliza poliza) {
        this.descripcion = descripcion;
        this.poliza = poliza;
        this.estado = EstadoRiesgo.ACTIVO;
    }

    public void cancelar() {
        if (!isActivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El riesgo ya está cancelado");
        }
        estado = EstadoRiesgo.CANCELADO;
    }

    public boolean isActivo() {
        return estado == EstadoRiesgo.ACTIVO;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public EstadoRiesgo getEstado() { return estado; }
    public Poliza getPoliza() { return poliza; }
}
