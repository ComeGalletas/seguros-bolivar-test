package com.seguros.polizas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Entity
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal canon;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    protected Poliza() {}

    public Poliza(TipoPoliza tipo, BigDecimal canon, BigDecimal prima) {
        this.tipo = tipo;
        this.estado = EstadoPoliza.ACTIVA;
        this.canon = canon;
        this.prima = prima;
    }

    /** Incrementa canon y prima según el IPC (p. ej. 0.05 = 5%) y marca la póliza como RENOVADA. */
    public void renovar(BigDecimal ipc) {
        if (isCancelada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede renovar una póliza cancelada");
        }
        BigDecimal factor = BigDecimal.ONE.add(ipc);
        canon = canon.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        prima = prima.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        estado = EstadoPoliza.RENOVADA;
    }

    /** Cancela la póliza y, en cascada, todos sus riesgos activos. */
    public void cancelar() {
        if (isCancelada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La póliza ya está cancelada");
        }
        estado = EstadoPoliza.CANCELADA;
        riesgos.stream().filter(Riesgo::isActivo).forEach(Riesgo::cancelar);
    }

    public Riesgo agregarRiesgo(String descripcion) {
        if (isCancelada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pueden agregar riesgos a una póliza cancelada");
        }
        if (tipo == TipoPoliza.INDIVIDUAL && !riesgos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Una póliza individual solo puede tener 1 riesgo");
        }
        Riesgo riesgo = new Riesgo(descripcion, this);
        riesgos.add(riesgo);
        return riesgo;
    }

    public boolean isCancelada() {
        return estado == EstadoPoliza.CANCELADA;
    }

    public Long getId() { return id; }
    public TipoPoliza getTipo() { return tipo; }
    public EstadoPoliza getEstado() { return estado; }
    public BigDecimal getCanon() { return canon; }
    public BigDecimal getPrima() { return prima; }
    public List<Riesgo> getRiesgos() { return Collections.unmodifiableList(riesgos); }
}
