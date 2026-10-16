package com.seguros.polizas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PolizaTest {

    private static final BigDecimal IPC = new BigDecimal("0.05");

    private static Poliza poliza(TipoPoliza tipo) {
        return new Poliza(tipo, new BigDecimal("1000.00"), new BigDecimal("100.00"));
    }

    @Test
    void renovarIncrementaCanonYPrimaPorIpc() {
        Poliza poliza = poliza(TipoPoliza.INDIVIDUAL);

        poliza.renovar(IPC);

        assertThat(poliza.getCanon()).isEqualByComparingTo("1050.00");
        assertThat(poliza.getPrima()).isEqualByComparingTo("105.00");
        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
    }

    @Test
    void noSePuedeRenovarPolizaCancelada() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.renovar(IPC))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cancelada");
        assertThat(poliza.getCanon()).isEqualByComparingTo("1000.00");
    }

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        poliza.agregarRiesgo("A");
        poliza.agregarRiesgo("B").cancelar();

        poliza.cancelar();

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(poliza.getRiesgos()).extracting(Riesgo::getEstado).containsOnly(EstadoRiesgo.CANCELADO);
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        Poliza poliza = poliza(TipoPoliza.INDIVIDUAL);
        poliza.cancelar();

        assertThatThrownBy(poliza::cancelar).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void polizaIndividualSoloAdmiteUnRiesgo() {
        Poliza poliza = poliza(TipoPoliza.INDIVIDUAL);
        poliza.agregarRiesgo("Único");

        assertThatThrownBy(() -> poliza.agregarRiesgo("Otro"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("1 riesgo");
        assertThat(poliza.getRiesgos()).hasSize(1);
    }

    @Test
    void polizaColectivaAdmiteVariosRiesgos() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        poliza.agregarRiesgo("A");
        poliza.agregarRiesgo("B");

        assertThat(poliza.getRiesgos()).hasSize(2).allMatch(Riesgo::isActivo);
    }

    @Test
    void noSePuedenAgregarRiesgosAPolizaCancelada() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.agregarRiesgo("A")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void noSePuedeCancelarRiesgoYaCancelado() {
        Riesgo riesgo = poliza(TipoPoliza.COLECTIVA).agregarRiesgo("A");
        riesgo.cancelar();

        assertThatThrownBy(riesgo::cancelar).isInstanceOf(ResponseStatusException.class);
    }
}
