package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.exception.InvalidTransactionException;
import com.duoc.banco_sociedad.exception.TemporaryTransactionException;
import com.duoc.banco_sociedad.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Component
public class TransactionProcessor implements ItemProcessor<Transaction, Transaction> {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    private int retryCounter = 0;

    @Override
    public Transaction process(Transaction transaction) {

        log.info(
                "Procesando transacción {} en hilo {}",
                transaction.getId(),
                Thread.currentThread().getName());

        // PRUEBA RETRY
        if (transaction.getId() == 8 && retryCounter < 2) {
            retryCounter++;

            log.warn(
                    "Falla temporal en transacción {}. Intento {}",
                    transaction.getId(),
                    retryCounter);

            throw new TemporaryTransactionException(
                    "Falla temporal simulada en transacción " + transaction.getId());
        }

        if (transaction.getAmount() == null) {
            throw new InvalidTransactionException(
                    "La transacción " + transaction.getId()
                            + " no contiene monto");
        }
        if (transaction.getAmount().signum() < 0) {

            log.warn(
                    "Transacción inválida detectada: {} con monto {}",
                    transaction.getId(),
                    transaction.getAmount());

            throw new InvalidTransactionException(
                    "Monto negativo en transacción "
                            + transaction.getId());
        }
        return transaction;
    }
}