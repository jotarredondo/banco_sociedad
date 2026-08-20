package com.duoc.banco_sociedad.listener;

import com.duoc.banco_sociedad.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionSkipListener
        implements SkipListener<Transaction, Transaction> {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionSkipListener.class);

    @Override
    public void onSkipInRead(Throwable throwable) {

        log.warn(
                "Error durante lectura de transacción: {}",
                throwable.getMessage()
        );
    }

    @Override
    public void onSkipInProcess(
            Transaction transaction,
            Throwable throwable) {

        log.warn(
                "Transacción {} omitida durante procesamiento. Motivo: {}",
                transaction.getId(),
                throwable.getMessage()
        );
    }

    @Override
    public void onSkipInWrite(
            Transaction transaction,
            Throwable throwable) {

        log.warn(
                "Transacción {} omitida durante escritura. Motivo: {}",
                transaction.getId(),
                throwable.getMessage()
        );
    }
}
