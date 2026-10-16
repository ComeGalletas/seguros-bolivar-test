package com.seguros.polizas.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seguros.polizas.config.ApiKeyFilter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas end-to-end sobre los datos de data.sql:
 * 1 INDIVIDUAL/ACTIVA (1 riesgo), 2 COLECTIVA/ACTIVA (2 riesgos),
 * 3 INDIVIDUAL/CANCELADA (1 riesgo cancelado), 555 COLECTIVA/RENOVADA (1 riesgo).
 * Cada prueba se revierte al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ExtendWith(OutputCaptureExtension.class)
class PolizaApiTest {

    @Autowired
    private MockMvc mvc;

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header(ApiKeyFilter.HEADER, "123456"));
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Nested
    class Seguridad {

        @Test
        void sinApiKeyResponde401() throws Exception {
            mvc.perform(get("/polizas")).andExpect(status().isUnauthorized());
        }

        @Test
        void apiKeyInvalidaResponde401() throws Exception {
            mvc.perform(get("/polizas").header(ApiKeyFilter.HEADER, "000000"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    class ListarPolizas {

        @Test
        void sinFiltrosDevuelveTodas() throws Exception {
            perform(get("/polizas")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(4)));
        }

        @Test
        void filtraPorTipoYEstado() throws Exception {
            perform(get("/polizas").param("tipo", "COLECTIVA").param("estado", "ACTIVA"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(2));
        }

        @Test
        void filtraSoloPorTipo() throws Exception {
            perform(get("/polizas").param("tipo", "INDIVIDUAL"))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].tipo", everyItem(is("INDIVIDUAL"))));
        }

        @Test
        void filtroInvalidoResponde400() throws Exception {
            perform(get("/polizas").param("tipo", "XYZ")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ListarRiesgos {

        @Test
        void devuelveRiesgosDeLaPoliza() throws Exception {
            perform(get("/polizas/2/riesgos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].polizaId", everyItem(is(2))));
        }

        @Test
        void polizaInexistenteResponde404() throws Exception {
            perform(get("/polizas/99/riesgos"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Póliza 99 no existe"));
        }
    }

    @Nested
    class Renovar {

        @Test
        void incrementaCanonYPrimaPorIpcYMarcaRenovada(CapturedOutput output) throws Exception {
            perform(post("/polizas/1/renovar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("RENOVADA"))
                    .andExpect(jsonPath("$.canon").value(1260000.00))
                    .andExpect(jsonPath("$.prima").value(89250.00));
            assertThat(output).contains("evento=ACTUALIZACION, polizaId=1");
        }

        @Test
        void polizaCanceladaResponde409() throws Exception {
            perform(post("/polizas/3/renovar"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("No se puede renovar una póliza cancelada"));
        }
    }

    @Nested
    class Cancelar {

        @Test
        void cancelaPolizaYTodosSusRiesgos() throws Exception {
            perform(post("/polizas/2/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADA"));
            perform(get("/polizas/2/riesgos"))
                    .andExpect(jsonPath("$[*].estado", everyItem(is("CANCELADO"))));
        }

        @Test
        void polizaYaCanceladaResponde409() throws Exception {
            perform(post("/polizas/3/cancelar")).andExpect(status().isConflict());
        }
    }

    @Nested
    class AgregarRiesgo {

        @Test
        void agregaRiesgoAPolizaColectiva() throws Exception {
            postJson("/polizas/2/riesgos", "{\"descripcion\":\"Nueva sede\"}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.estado").value("ACTIVO"))
                    .andExpect(jsonPath("$.polizaId").value(2));
            perform(get("/polizas/2/riesgos")).andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        void polizaIndividualResponde409() throws Exception {
            postJson("/polizas/1/riesgos", "{\"descripcion\":\"Otro\"}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Solo se pueden agregar riesgos a pólizas de tipo COLECTIVA"));
        }

        @Test
        void polizaColectivaCanceladaResponde409() throws Exception {
            perform(post("/polizas/2/cancelar"));
            postJson("/polizas/2/riesgos", "{\"descripcion\":\"Otro\"}").andExpect(status().isConflict());
        }

        @Test
        void descripcionVaciaResponde400() throws Exception {
            postJson("/polizas/2/riesgos", "{\"descripcion\":\"\"}").andExpect(status().isBadRequest());
        }
    }

    @Nested
    class CancelarRiesgo {

        @Test
        void cancelaRiesgo() throws Exception {
            perform(post("/riesgos/1/cancelar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("CANCELADO"));
        }

        @Test
        void riesgoYaCanceladoResponde409() throws Exception {
            perform(post("/riesgos/4/cancelar"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("El riesgo ya está cancelado"));
        }

        @Test
        void riesgoInexistenteResponde404() throws Exception {
            perform(post("/riesgos/99/cancelar"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Riesgo 99 no existe"));
        }
    }

    @Nested
    class CoreMock {

        @Test
        void registraEventoEnLogs(CapturedOutput output) throws Exception {
            postJson("/core-mock/evento", "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}")
                    .andExpect(status().isAccepted());
            assertThat(output).contains("[CORE-MOCK]", "evento=ACTUALIZACION, polizaId=555");
        }

        @Test
        void eventoInvalidoResponde400() throws Exception {
            postJson("/core-mock/evento", "{\"polizaId\":555}").andExpect(status().isBadRequest());
        }
    }
}
