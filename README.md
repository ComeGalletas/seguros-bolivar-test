# API de Gestión de Pólizas

API REST en **Spring Boot 3.5** (Java 21+) para gestionar pólizas y sus riesgos, con base de datos H2 en memoria precargada.

## Requisitos

- JDK 21 o superior
- Maven 3.9+

## Ejecución

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. Para generar y ejecutar el JAR:

```bash
mvn package
java -jar target/polizas-api-1.0.0.jar
```

## Pruebas

```bash
mvn test
```

29 pruebas: unitarias del dominio (`PolizaTest`) e integración de todos los endpoints con MockMvc (`PolizaApiTest`).

### Cobertura

```bash
mvn verify
```

Ejecuta las pruebas con [JaCoCo](https://www.jacoco.org/) y genera el reporte en `target/site/jacoco/index.html` (también `jacoco.xml` y `jacoco.csv` para herramientas de CI). Cobertura actual: 98,5 % de líneas y 100 % de ramas; solo queda fuera `PolizasApplication.main()`.

## Seguridad

Todas las peticiones requieren el header `x-api-key: 123456` (configurable en `polizas.api-key`). Sin él la respuesta es `401`.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/polizas?tipo=&estado=` | Lista pólizas. Filtros opcionales: `tipo` (`INDIVIDUAL`, `COLECTIVA`) y `estado` (`ACTIVA`, `RENOVADA`, `CANCELADA`) |
| GET | `/polizas/{id}/riesgos` | Riesgos de una póliza |
| POST | `/polizas/{id}/renovar` | Incrementa canon y prima en el IPC y pasa la póliza a `RENOVADA` |
| POST | `/polizas/{id}/cancelar` | Cancela la póliza y todos sus riesgos |
| POST | `/polizas/{id}/riesgos` | Agrega un riesgo (solo pólizas `COLECTIVA`). Body: `{"descripcion": "..."}` |
| POST | `/riesgos/{id}/cancelar` | Cancela un riesgo |
| POST | `/core-mock/evento` | Mock del CORE: registra el evento en logs. Body: `{"evento": "ACTUALIZACION", "polizaId": 555}` |

### Ejemplos

```bash
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"
```

```bash
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
```

```bash
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" -d '{"descripcion":"Bodega norte"}' http://localhost:8080/polizas/2/riesgos
```

```bash
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
```

## Reglas de negocio

- Una póliza individual solo puede tener 1 riesgo.
- Solo se pueden agregar riesgos a pólizas de tipo `COLECTIVA`.
- No se puede renovar una póliza cancelada.
- Cancelar una póliza cancela todos sus riesgos.
- No se pueden agregar riesgos a una póliza cancelada ni cancelar dos veces una póliza o un riesgo.

El IPC se configura en `polizas.ipc` (por defecto `0.05`, es decir 5 %). Los montos se redondean a 2 decimales.

Las operaciones que modifican datos (renovar, cancelar, agregar o cancelar riesgo) dejan en el log un intento de envío al CORE mediante `CoreService`, el mismo que usa `/core-mock/evento`.

### Errores

Las respuestas de error siguen el formato RFC 7807 (`application/problem+json`):

| Código | Caso |
|---|---|
| 400 | Body inválido o filtro con valor desconocido |
| 401 | Header `x-api-key` ausente o inválido |
| 404 | Póliza o riesgo inexistente |
| 409 | Violación de una regla de negocio |

## Datos iniciales (`data.sql`)

| Id | Tipo | Estado | Canon | Prima | Riesgos |
|---|---|---|---|---|---|
| 1 | INDIVIDUAL | ACTIVA | 1.200.000 | 85.000 | 1 |
| 2 | COLECTIVA | ACTIVA | 5.400.000 | 320.000 | 2 |
| 3 | INDIVIDUAL | CANCELADA | 900.000 | 60.000 | 1 (cancelado) |
| 555 | COLECTIVA | RENOVADA | 2.500.000 | 150.000 | 1 |

Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:polizas`, usuario `sa`, sin contraseña).

## Estructura

```
src/main/java/com/seguros/polizas
├── config/        ApiKeyFilter, PolizasProperties
├── controller/    PolizaController, RiesgoController, CoreMockController, dto/
├── domain/        Poliza, Riesgo y enums (las reglas de negocio viven en las entidades)
├── repository/    PolizaRepository, RiesgoRepository
└── service/       PolizaService, RiesgoService, CoreService
```
