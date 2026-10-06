# Banco Sociedad - Parte 1 EFT
## Migración de Procesos Batch con Spring Batch

Proyecto desarrollado para la Parte 1 de la Evaluación Final Transversal de
Desarrollo Backend III.

El objetivo es migrar procesos batch asociados al procesamiento de información
financiera utilizando Spring Boot y Spring Batch, aplicando validación de datos,
tolerancia a fallos, procesamiento concurrente y generación de resultados
persistidos en base de datos.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 4.1.0
- Spring Batch
- Spring Data JPA
- Hibernate
- H2 Database
- Maven
- Lombok

---

## Procesos implementados

El proyecto contiene tres Jobs independientes.

### 1. Reporte de Transacciones Diarias

Job:

dailyTransactionJob

Step:

processTransactionsStep

Archivo procesado:

movimientos_financieros_diarios.csv

El proceso:

- Lee las transacciones desde CSV.
- Valida monto, fecha y tipo de transacción.
- Reconoce cuatro formatos de fecha:
    - yyyy-MM-dd
    - yyyy/MM/dd
    - dd-MM-yyyy
    - dd/MM/yyyy
- Descarta fechas inválidas.
- Descarta montos nulos.
- Descarta tipos distintos de credito y debito.
- Detecta montos negativos como transacciones anómalas.
- Persiste los registros válidos en base de datos.
- Genera un resumen de ejecución al finalizar.

Resultado obtenido con el dataset entregado:

Registros leídos: 1000
Registros persistidos: 480
Registros filtrados: 520
Registros omitidos por skip: 0
Estado Job: COMPLETED

---

### 2. Cálculo de Intereses

Job:

quarterlyInterestJob

Step:

calculateQuarterlyInterestStep

Archivo procesado:

intereses_trimestrales.csv

El proceso valida:

- Saldo nulo.
- Edad nula.
- Edad fuera del rango definido entre 18 y 110 años.
- Nombre vacío o Unknown.
- Tipo de cuenta inválido.
- Registros duplicados.

Los tipos válidos son:

ahorro
prestamo
hipoteca

También se calcula:

interesCalculado
saldoFinal

### Tasas utilizadas

Para efectos de la implementación se definieron las siguientes tasas:

Tipo | Tasa
Ahorro | 1%
Préstamo | 2%
Hipoteca | 1.5%

Estas tasas corresponden a una decisión de diseño del proyecto, debido a que
los archivos proporcionados no incluyen una regla específica de cálculo de
intereses.

La detección de duplicados se realiza comparando la combinación:

cuentaId + nombre + saldo + edad + tipo

De esta forma, cuentas repetidas no son eliminadas cuando contienen información
diferente.

Resultado final:

Registros persistidos: 396
Duplicados válidos descartados: 2
Estado Job: COMPLETED

Nota: Las instrucciones generales de la EFT mencionan un cálculo mensual,
mientras que el dataset proporcionado corresponde a intereses_trimestrales.csv.
La implementación sigue el archivo oficial suministrado para el ejercicio.

---

### 3. Generación de Estados Financieros Anuales

Job:

annualStatementJob

Step:

generateAnnualStatementStep

Archivo procesado:

estados_financieros_anuales.csv

El proceso:

- Lee los movimientos financieros anuales.
- Valida fechas.
- Valida montos nulos.
- Valida tipos de transacción.
- Normaliza depósito a deposito.
- Reemplaza descripciones faltantes por SIN DESCRIPCION.
- Persiste los movimientos válidos.
- Genera un resumen anual por cuenta.

El resumen incluye:

Cuenta
Año
Depósitos
Egresos
Saldo
Cantidad de movimientos

Los egresos consideran:

retiro
compra
pago

La agregación se realiza después de persistir los movimientos válidos.

---

## Manejo de errores

Los tres procesos utilizan tolerancia a fallos mediante Spring Batch.

Configuración utilizada:

.faultTolerant()
.retry(TransientDataAccessException.class)
.retryLimit(3)

Esto permite realizar hasta tres intentos frente a fallos transitorios de acceso
a datos, como problemas temporales de conexión con la base de datos.

---

## Política de reejecución

El proceso de transacciones diarias incluye una política adicional para manejar
fallos críticos.

El DailyTransactionJobListener revisa el estado final del Job.

Si termina en:

FAILED

se utiliza:

jobOperator.restart(jobExecution);

para solicitar automáticamente la reejecución del Job.

Esta política complementa los reintentos utilizados para fallos temporales.

---

## Optimización y procesamiento concurrente

Para el proceso de transacciones diarias se implementó procesamiento
multihilo mediante:

SimpleAsyncTaskExecutor

y un:

SynchronizedItemStreamReader

para proteger el acceso concurrente al reader.

Se realizaron pruebas manteniendo el mismo tamaño de chunk y variando la
cantidad de hilos.

Cantidad de hilos | Tiempo de ejecución
1 | 868 ms
3 | 715 ms
5 | 1073 ms

La configuración seleccionada fue:

executor.setConcurrencyLimit(3);

Los 3 hilos entregaron el mejor tiempo entre las configuraciones evaluadas.

En comparación con la ejecución de un solo hilo, la mejora observada fue
aproximadamente de un 17.6%.

El uso de 5 hilos aumentó el tiempo de ejecución, probablemente debido al
overhead adicional asociado a la administración de concurrencia para el volumen
de datos utilizado.

---

## Integridad y consistencia de datos

Durante la migración se aplicaron reglas para evitar persistir información que
pueda afectar la consistencia de los resultados.

Entre ellas:

- Validación de campos obligatorios.
- Soporte para múltiples formatos de fecha.
- Normalización de valores.
- Control de tipos válidos.
- Validación de rangos.
- Identificación de duplicados.
- Registro de anomalías.
- Manejo de descripciones faltantes.
- Retry para errores transitorios.
- Reejecución frente a fallos críticos.

Las reglas que no estaban definidas explícitamente en los datos entregados,
como las tasas de interés y el rango de edad, se documentan como decisiones de
diseño de la solución.

---

## Estructura principal

src/main/java/com/duoc/banco_sociedad
|
+-- config
|   +-- DailyTransactionBatchConfig.java
|   +-- QuarterlyInterestBatchConfig.java
|   +-- AnnualStatementBatchConfig.java
|
+-- listener
|   +-- DailyTransactionJobListener.java
|   +-- AnnualStatementJobListener.java
|
+-- model
|   +-- DailyTransaction.java
|   +-- QuarterlyInterest.java
|   +-- AnnualStatement.java
|
+-- processor
|   +-- DailyTransactionProcessor.java
|   +-- QuarterlyInterestProcessor.java
|   +-- AnnualStatementProcessor.java
|
+-- repository
|   +-- TransactionRepository.java
|   +-- AnnualStatementRepository.java
|
+-- utils
|   +-- ParseDate.java
+-- BancoSociedadApplication.java

Los archivos CSV se encuentran en:

src/main/resources/data

---

## Ejecución

El Job que se desea ejecutar se selecciona en:

src/main/resources/application.properties

Por ejemplo:

spring.batch.job.name=dailyTransactionJob

Otros Jobs disponibles:

spring.batch.job.name=quarterlyInterestJob

spring.batch.job.name=annualStatementJob

Desde IntelliJ IDEA se puede ejecutar directamente:

BancoSociedadApplication

También puede ejecutarse desde consola en Windows:

mvnw.cmd spring-boot:run

---

## Resultado

La solución permite migrar los tres procesos batch definidos utilizando Spring
Batch, manteniendo separación entre Jobs, validación y transformación de datos,
persistencia, tolerancia a fallos y optimización del procesamiento.

Además, se incorporaron mejoras respecto de la versión inicial del proyecto:

- Compatibilidad con los cuatro formatos de fecha presentes en los archivos.
- Validaciones adicionales de consistencia.
- Detección de registros duplicados.
- Comparación real de configuraciones de concurrencia.
- Selección justificada de tres hilos.
- Reintentos frente a fallos temporales.
- Política de reejecución frente a fallos críticos.
