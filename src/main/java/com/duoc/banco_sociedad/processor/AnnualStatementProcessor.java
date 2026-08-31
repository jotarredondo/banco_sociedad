package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.AnnualStatement;
import com.duoc.banco_sociedad.model.AnnualTransaction;
import com.duoc.banco_sociedad.repository.AnnualTransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AnnualStatementProcessor
        implements ItemProcessor<Long, AnnualStatement> {

    private static final Logger log =
            LoggerFactory.getLogger(AnnualStatementProcessor.class);

    private final AnnualTransactionRepository annualTransactionRepository;

    public AnnualStatementProcessor(
            AnnualTransactionRepository annualTransactionRepository) {

        this.annualTransactionRepository = annualTransactionRepository;
    }

    @Override
    public AnnualStatement process(Long accountId) {

        List<AnnualTransaction> transactions =
                annualTransactionRepository.findByAccountId(accountId);

        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        int year = transactions.get(0)
                .getDate()
                .getYear();

        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;

        for (AnnualTransaction transaction : transactions) {

            String type = transaction.getTransactionType();
            BigDecimal amount = transaction.getAmount();

            if ("deposito".equals(type)) {

                totalDeposits =
                        totalDeposits.add(amount);

            } else if (
                    "retiro".equals(type)
                            || "compra".equals(type)
                            || "pago".equals(type)) {

                totalWithdrawals =
                        totalWithdrawals.add(amount);
            }
        }

        BigDecimal finalBalance =
                totalDeposits.subtract(totalWithdrawals);

        AnnualStatement statement =
                new AnnualStatement();

        statement.setAccountId(accountId);
        statement.setYear_(year);
        statement.setTotalDeposits(totalDeposits);
        statement.setTotalWithdrawals(totalWithdrawals);
        statement.setFinalBalance(finalBalance);

        if (finalBalance.compareTo(BigDecimal.ZERO) >= 0) {
            statement.setStatus("OK");
        } else {
            statement.setStatus("REVIEW");
        }

        log.info(
                "Estado anual calculado | Cuenta: {} | Año: {} | Depósitos: {} | Egresos: {} | Saldo final: {} | Estado: {}",
                statement.getAccountId(),
                statement.getYear_(),
                statement.getTotalDeposits(),
                statement.getTotalWithdrawals(),
                statement.getFinalBalance(),
                statement.getStatus()
        );

        return statement;
    }
}
