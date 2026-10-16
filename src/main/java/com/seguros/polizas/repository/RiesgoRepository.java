package com.seguros.polizas.repository;

import com.seguros.polizas.domain.Riesgo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {

    List<Riesgo> findByPolizaIdOrderById(Long polizaId);
}
