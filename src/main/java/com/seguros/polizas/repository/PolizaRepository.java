package com.seguros.polizas.repository;

import com.seguros.polizas.domain.EstadoPoliza;
import com.seguros.polizas.domain.Poliza;
import com.seguros.polizas.domain.TipoPoliza;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    /** Filtros opcionales: un parámetro {@code null} no restringe el resultado. */
    @Query("""
            SELECT p FROM Poliza p
            WHERE (:tipo IS NULL OR p.tipo = :tipo)
              AND (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.id""")
    List<Poliza> buscar(TipoPoliza tipo, EstadoPoliza estado);
}
