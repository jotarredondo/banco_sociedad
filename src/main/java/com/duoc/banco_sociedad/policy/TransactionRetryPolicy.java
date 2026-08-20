package com.duoc.banco_sociedad.policy;

import com.duoc.banco_sociedad.exception.TemporaryTransactionException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.stereotype.Component;

@Component
public class TransactionRetryPolicy {

    public RetryPolicy retryPolicy() {

        return RetryPolicy.builder()
                .maxRetries(3)
                .includes(TemporaryTransactionException.class)
                .build();
    }
}
