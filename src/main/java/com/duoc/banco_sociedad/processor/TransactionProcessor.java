package com.duoc.banco_sociedad.processor;


import com.duoc.banco_sociedad.model.Transaction;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor
        implements ItemProcessor<Transaction, Transaction> {

    @Override
    public Transaction process(Transaction transaction) {

        if (transaction.getAmount() == null) {
            return null;
        }

        if (transaction.getAmount().signum() < 0) {
            System.out.println(
                    "Transacción anómala: " + transaction.getId()
            );
        }

        return transaction;
    }
}
