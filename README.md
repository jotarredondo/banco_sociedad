# Banco Sociedad - Procesamiento Batch con Spring Batch

## Descripción

Este proyecto implementa una solución de procesamiento Batch para la modernización de procesos legacy asociados al Banco XYZ.

La solución utiliza Spring Batch para leer información desde archivos CSV, procesar y validar los datos, aplicar reglas de negocio y persistir los resultados en una base de datos relacional.

Durante la Semana 2 se incorporaron técnicas de optimización, procesamiento paralelo, tolerancia a fallos, políticas de omisión y reintento, listeners y logging para mejorar la resiliencia y trazabilidad de los procesos Batch.

---

## Objetivo

Modernizar tres procesos Batch legacy del Banco XYZ:

1. Reporte de transacciones diarias.
2. Cálculo de intereses mensuales.
3. Generación de estados de cuenta anuales.

Cada proceso fue implementado como un Job independiente de Spring Batch.

---

## Tecnologías utilizadas

* Java 21
* Spring Boot 4.1.0
* Spring Batch 6
* Spring Data JPA
* Hibernate
* H2 Database
* Maven
* Lombok
* SLF4J

---

## Estructura del proyecto

```text
src/main/java/com/duoc/banco_sociedad

├── config
│   ├── DailyTransactionJobConfig.java
│   ├── MonthlyInterestJobConfig.java
│   ├── AnnualStatementJobConfig.java
│   └── TaskExecutorConfig.java
│
├── exception
│   ├── InvalidTransactionException.java
│   └── TemporaryTransactionException.java
│
├── listener
│   ├── BatchJobListener.java
│   ├── BatchStepListener.java
│   └── TransactionSkipListener.java
│
├── model
│   ├── Transaction.java
│   ├── Account.java
│   └── AnnualStatement.java
│
├── policy
│   ├── TransactionSkipPolicy.java
│   └── TransactionRetryPolicy.java
│
├── processor
│   ├── TransactionProcessor.java
│   ├── AccountInterestProcessor.java
│   └── AnnualStatementProcessor.java
│
├── repository
│   └── TransactionRepository.java
│
└── BancoSociedadApplication.java
```

Recursos:

```text
src/main/resources

├── data
│   ├── transactions.csv
│   ├── accounts.csv
│   └── annual_statements.csv
│
└── application.properties
```

---

# Arquitectura Batch

Cada proceso sigue la arquitectura básica de Spring Batch:

```text
CSV
 ↓
ItemReader
 ↓
ItemProcessor
 ↓
ItemWriter
 ↓
Base de datos
```

Cada flujo de procesamiento se ejecuta dentro de un `Step`, y cada Step pertenece a un Job independiente.

La solución implementa:

```text
dailyTransactionJob
    └── processTransactionsStep

monthlyInterestJob
    └── calculateMonthlyInterestStep

annualStatementJob
    └── generateAnnualStatementStep
```

---

# Procesamiento por Chunks

Los Steps fueron configurados utilizando chunks de tamaño 5:

```text
.<Entidad, Entidad>chunk(5)
```

Esto permite procesar los registros en grupos pequeños, reduciendo el uso de memoria y permitiendo administrar de mejor forma las transacciones y los errores.

---

# Procesamiento paralelo

La aplicación utiliza un `ThreadPoolTaskExecutor` con 3 hilos de ejecución:

```text
executor.setCorePoolSize(3);
executor.setMaxPoolSize(3);
```

Los hilos utilizan el siguiente prefijo:

```text
Banco-Batch-
```

Durante la ejecución se pueden observar:

```text
Banco-Batch-1
Banco-Batch-2
Banco-Batch-3
```

procesando distintos registros en paralelo.

---

# Lectura segura en entornos multihilo

Los archivos CSV se leen utilizando `FlatFileItemReader`.

Como este componente no está diseñado para acceso concurrente, fue envuelto mediante:

```text
SynchronizedItemStreamReader
```

para permitir una ejecución segura dentro de Steps multihilo.

---

# Jobs implementados

## 1. dailyTransactionJob

Procesa las transacciones diarias contenidas en:

```text
transactions.csv
```

Sus principales responsabilidades son:

* Leer transacciones desde CSV.
* Validar los datos.
* Detectar montos inválidos.
* Procesar los registros en paralelo.
* Omitir datos inconsistentes mediante una política de Skip.
* Reintentar fallos temporales.
* Persistir los registros válidos.

### Manejo de transacciones inválidas

Cuando una transacción presenta un monto negativo, el `TransactionProcessor` genera:

```text
InvalidTransactionException
```

Esta excepción es manejada mediante:

```text
TransactionSkipPolicy
```

permitiendo omitir el registro sin detener el Job.

Ejemplo:

```text
Transacción inválida detectada: 5 con monto -1250.00
Registro omitido. Error: Monto negativo en transacción 5
```

---

## Política de Skip

La política:

```text
TransactionSkipPolicy
```

permite omitir hasta un máximo determinado de registros inválidos.

El flujo es:

```text
Dato inválido
      ↓
InvalidTransactionException
      ↓
TransactionSkipPolicy
      ↓
SKIP
      ↓
continúa el Job
```

---

## SkipListener

El componente:

```text
TransactionSkipListener
```

registra información acerca de los elementos que fueron omitidos.

Ejemplo:

```text
Transacción 5 omitida durante procesamiento.
Motivo: Monto negativo en transacción 5
```

Esto permite identificar posteriormente qué registros no fueron procesados y por qué motivo.

---

## Política de Retry

También se implementó una política de reintento:

```text
TransactionRetryPolicy
```

para manejar errores temporales.

Los errores temporales utilizan:

```text
TemporaryTransactionException
```

La política permite hasta 3 intentos.

Ejemplo:

```text
Falla temporal en transacción 8. Intento 1
Falla temporal en transacción 8. Intento 2
Procesando transacción 8
```

Luego del tercer intento, la transacción puede continuar normalmente.

Esto permite diferenciar entre:

```text
Error permanente de datos
       ↓
SKIP

Error temporal
       ↓
RETRY
```

---

# 2. monthlyInterestJob

Procesa las cuentas almacenadas en:

```text
accounts.csv
```

Este Job:

* Lee las cuentas.
* Procesa cuentas de ahorro y préstamos.
* Calcula los intereses.
* Actualiza el saldo correspondiente.
* Utiliza chunks de 5.
* Ejecuta el procesamiento mediante 3 hilos.
* Persiste los resultados en la base de datos.

El cálculo aplicado corresponde a:

```text
Interés = Saldo × Tasa de interés
```

La ejecución utiliza:

```text
Banco-Batch-1
Banco-Batch-2
Banco-Batch-3
```

para distribuir el procesamiento.

---

# 3. annualStatementJob

Procesa:

```text
annual_statements.csv
```

Este Job:

* Lee los datos anuales de las cuentas.
* Evalúa el saldo final.
* Procesa los registros utilizando 3 hilos.
* Utiliza chunks de tamaño 5.
* Persiste los estados de cuenta procesados.

Cuando el saldo final es negativo, el estado queda marcado como:

```text
REVIEW
```

En caso contrario:

```text
OK
```

Ejemplo:

```text
Estado anual requiere revisión: cuenta 1003
```

---

# TaskExecutor

El procesamiento paralelo se configura en:

```text
TaskExecutorConfig.java
```

La configuración utiliza:

```text
executor.setCorePoolSize(3);
executor.setMaxPoolSize(3);
executor.setQueueCapacity(20);
executor.setThreadNamePrefix("Banco-Batch-");
```

De esta forma se limita la aplicación a un máximo de 3 hilos de procesamiento simultáneo.

---

# Listeners

## BatchJobListener

Permite registrar el inicio y finalización de cada Job.

Ejemplo:

```text
Iniciando Job: dailyTransactionJob | ID ejecución: 1
```

y:

```text
Finalizando Job: dailyTransactionJob | Estado: COMPLETED
```

---

## BatchStepListener

Permite registrar el inicio y el resultado final de cada Step.

Ejemplo:

```text
Finalizando Step: processTransactionsStep |
Leídos: 15 |
Escritos: 14 |
Omitidos: 1 |
Estado: COMPLETED
```

Esto facilita la trazabilidad, monitoreo y análisis de cada proceso Batch.

---

# Manejo de errores y tolerancia a fallos

La solución utiliza diferentes estrategias de tolerancia a fallos:

```text
faultTolerant()
SkipPolicy
RetryPolicy
SkipListener
JobExecutionListener
StepExecutionListener
```

El objetivo es que errores controlados no provoquen necesariamente la detención completa del procesamiento.

---

# Logging

Se utiliza SLF4J para registrar los eventos principales de procesamiento.

Los logs permiten visualizar:

* Inicio y término de Jobs.
* Inicio y término de Steps.
* Cantidad de registros leídos.
* Cantidad de registros escritos.
* Cantidad de registros omitidos.
* Hilo utilizado para procesar cada registro.
* Datos inválidos.
* Reintentos.
* Estado final del proceso.

---

# Configuración de la base de datos

Para esta implementación se utiliza H2 en memoria.

Configuración:

```properties
spring.datasource.url=jdbc:h2:mem:bancodb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

spring.batch.jdbc.initialize-schema=always
```

---

# Selección del Job

El Job que se desea ejecutar se selecciona desde:

```text
application.properties
```

## Transacciones diarias

```properties
spring.batch.job.name=dailyTransactionJob
```

## Intereses mensuales

```properties
spring.batch.job.name=monthlyInterestJob
```

## Estados de cuenta anuales

```properties
spring.batch.job.name=annualStatementJob
```

Se debe mantener solo un Job seleccionado para cada ejecución de prueba.

---

# Ejecución

1. Clonar o descargar el proyecto.
2. Abrirlo en IntelliJ IDEA.
3. Verificar que Maven descargue todas las dependencias.
4. Seleccionar el Job deseado en `application.properties`.
5. Ejecutar:

```text
BancoSociedadApplication
```

6. Revisar los logs de consola.

---

# Resultado esperado

Una ejecución correcta muestra:

```text
Estado: COMPLETED
```

Por ejemplo, para el Job de transacciones:

```text
Finalizando Step: processTransactionsStep |
Leídos: 15 |
Escritos: 14 |
Omitidos: 1 |
Estado: COMPLETED

Finalizando Job: dailyTransactionJob |
Estado: COMPLETED
```

Para intereses mensuales:

```text
Leídos: 15 |
Escritos: 15 |
Omitidos: 0 |
Estado: COMPLETED
```

Para estados anuales:

```text
Leídos: 15 |
Escritos: 15 |
Omitidos: 0 |
Estado: COMPLETED
```

---

# Resultado final

La solución implementa los tres procesos Batch solicitados:

```text
dailyTransactionJob
monthlyInterestJob
annualStatementJob
```

Los tres Jobs utilizan:

```text
Chunk size = 5
3 hilos de procesamiento
SynchronizedItemStreamReader
JobListener
StepListener
SLF4J Logging
```

Además, `dailyTransactionJob` implementa:

```text
faultTolerant()
SkipPolicy
RetryPolicy
SkipListener
```

permitiendo demostrar tolerancia a fallos, manejo de datos inválidos y recuperación ante fallas temporales.

La solución mantiene cada proceso Batch en una configuración independiente para mejorar la organización, legibilidad y mantenibilidad del código.
