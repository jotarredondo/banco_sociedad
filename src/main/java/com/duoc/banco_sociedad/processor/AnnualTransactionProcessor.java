package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.exception.InvalidTransactionException;
import com.duoc.banco_sociedad.model.AnnualTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AnnualTransactionProcessor
        implements ItemProcessor<AnnualTransaction, AnnualTransaction> {

    private static final Logger log =
            LoggerFactory.getLogger(AnnualTransactionProcessor.class);

    @Override
    public AnnualTransaction process(AnnualTransaction transaction) {

        if (transaction.getDate() == null) {
            throw new InvalidTransactionException(
                    "Fecha inválida para cuenta "
                            + transaction.getAccountId()
            );
        }

        if (transaction.getAmount() == null) {
            throw new InvalidTransactionException(
                    "Monto vacío para cuenta " + transaction.getAccountId());
        }

            if (transaction.getAmount()
                    .compareTo(BigDecimal.ZERO) <= 0) {

                throw new InvalidTransactionException(
                        "Monto inválido para cuenta "
                                + transaction.getAccountId()
                                + ": "
                                + transaction.getAmount()
                );
            }

            String type = transaction
                    .getTransactionType()
                    .toLowerCase()
                    .trim();

            // Normalización del legacy
            if (type.equals("depósito")) {
                type = "deposito";
            }

            switch (type) {
                case "deposito":
                case "retiro":
                case "compra":
                case "pago":
                    break;

                default:
                    throw new InvalidTransactionException(
                            "Tipo de transacción inválido: "
                                    + type
                    );
            }

            transaction.setTransactionType(type);

            log.info(
                    "Transacción anual válida | Cuenta: {} | Tipo: {} | Monto: {} | Hilo: {}",
                    transaction.getAccountId(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    Thread.currentThread().getName()
            );

            return transaction;
        }
    }