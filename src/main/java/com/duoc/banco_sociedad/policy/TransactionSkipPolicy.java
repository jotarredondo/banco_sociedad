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

    private static final long MAX_SKIP_COUNT = 5;

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) {

        boolean shouldSkip =
                throwable instanceof InvalidTransactionException
                        && skipCount < MAX_SKIP_COUNT;

        if (shouldSkip) {
            log.warn(
                    "Registro omitido. Error: {} | Skip número: {}",
                    throwable.getMessage(),
                    skipCount + 1
            );
        }

        return shouldSkip;
    }
}