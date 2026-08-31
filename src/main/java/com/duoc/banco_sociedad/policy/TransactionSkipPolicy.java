package com.duoc.banco_sociedad.policy;

import com.duoc.banco_sociedad.exception.InvalidTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.stereotype.Component;


@Component
public class TransactionSkipPolicy implements SkipPolicy {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionSkipPolicy.class);

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) {

        if (throwable instanceof InvalidTransactionException) {

            log.warn(
                    "Registro inválido omitido. Error: {} | Total omitidos hasta ahora: {}",
                    throwable.getMessage(),
                    skipCount + 1);
            return true;
        }

        return false;
    }
}