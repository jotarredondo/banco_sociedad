# Banco Sociedad - Procesamiento Batch con Spring Batch

## Objetivo

Este proyecto implementa una solución básica de procesamiento por lotes utilizando Spring Batch, con el propósito de modernizar procesos asociados a un sistema bancario legacy.

La aplicación procesa información desde archivos CSV, aplica validaciones y transformaciones mediante `ItemProcessor`, y persiste los resultados utilizando JPA y una base de datos H2 para fines de prueba.

## Tecnologías utilizadas

* Java 21
* Spring Boot
* Spring Batch
* Spring Data JPA
* Hibernate
* H2 Database
* Maven
* Lombok

## Estructura del proyecto

```text
src/main/java/com/duoc/banco_sociedad
├── config
│   └── BatchConfig.java
│
├── model
│   ├── Transaction.java
│   ├── Account.java
│   └── AnnualStatement.java
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

src/main/resources
├── data
│   ├── transactions.csv
│   ├── accounts.csv
│   └── annual_statements.csv
│
└── application.properties
```

## Arquitectura Batch

Cada proceso sigue la arquitectura básica de Spring Batch:

```text
Archivo CSV
    ↓
ItemReader
    ↓
ItemProcessor
    ↓
ItemWriter
    ↓
Base de datos
```

Cada flujo se ejecuta dentro de un `Step`, el cual pertenece a un `Job`.

## Jobs implementados

### 1. dailyTransactionJob

Procesa las transacciones diarias contenidas en `transactions.csv`.

El flujo permite:

* Leer transacciones desde CSV.
* Validar que el monto exista.
* Detectar transacciones con montos negativos.
* Persistir los registros procesados.

En caso de detectar una transacción con monto negativo, el sistema muestra un mensaje indicando que se trata de una transacción anómala.

### 2. monthlyInterestJob

Procesa las cuentas almacenadas en `accounts.csv`.

El flujo permite:

* Leer cuentas de ahorro y préstamos.
* Obtener el saldo y la tasa de interés.
* Calcular el interés correspondiente.
* Actualizar el saldo final.
* Persistir el resultado.

El cálculo utilizado es:

```text
Interés = Saldo × Tasa de interés
```

### 3. annualStatementJob

Procesa los estados de cuenta contenidos en `annual_statements.csv`.

El flujo permite:

* Leer información anual de las cuentas.
* Evaluar el saldo final.
* Clasificar el estado como `OK` o `REVIEW`.
* Persistir el estado de cuenta procesado.

Cuando el saldo final es negativo, el registro queda marcado como:

```text
REVIEW
```

## Selección del Job

El Job que se ejecutará puede configurarse mediante `application.properties`.

Ejemplo:

```properties
spring.batch.job.name=dailyTransactionJob
```

Para ejecutar el Job de intereses:

```properties
spring.batch.job.name=monthlyInterestJob
```

Para ejecutar el Job anual:

```properties
spring.batch.job.name=annualStatementJob
```

## Configuración de H2

El proyecto utiliza una base de datos H2 en memoria para realizar las pruebas del procesamiento Batch.

Configuración utilizada:

```properties
spring.datasource.url=jdbc:h2:mem:bancodb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

spring.batch.jdbc.initialize-schema=always
```

## Ejecución del proyecto

1. Abrir el proyecto en IntelliJ IDEA.
2. Verificar el Job deseado en `application.properties`.
3. Ejecutar la clase:

```text
BancoSociedadApplication
```

4. Revisar la consola.

Una ejecución exitosa mostrará un resultado similar a:

```text
Job: [dailyTransactionJob] launched
Executing step: [processTransactionsStep]

Step: [processTransactionsStep] executed
Job: [dailyTransactionJob] completed
status: [COMPLETED]
```

## Manejo de errores y validaciones

Los `ItemProcessor` realizan validaciones antes de persistir la información.

Ejemplos implementados:

* Registros sin monto pueden ser descartados.
* Transacciones con montos negativos son identificadas como anómalas.
* Estados anuales con saldo negativo son clasificados como `REVIEW`.

Esto permite identificar información inconsistente proveniente de sistemas legacy sin detener necesariamente el procesamiento completo.

## Resultado

La aplicación implementa los tres procesos Batch solicitados:

```text
dailyTransactionJob
monthlyInterestJob
annualStatementJob
```

Cada Job utiliza los componentes principales de Spring Batch:

```text
Job
↓
Step
↓
ItemReader
↓
ItemProcessor
↓
ItemWriter
```

permitiendo leer, transformar y persistir datos provenientes de archivos CSV.
