package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import com.duoc.banco_sociedad.exception.InvalidTransactionException;

import java.math.RoundingMode;

@Component
public class AccountInterestProcessor
        implements ItemProcessor<Account, Account> {

    private static final Logger log =
            LoggerFactory.getLogger(AccountInterestProcessor.class);

    private static final BigDecimal SAVINGS_RATE =
            new BigDecimal("0.01");

    private static final BigDecimal LOAN_RATE =
            new BigDecimal("0.02");

    private static final BigDecimal MORTGAGE_RATE =
            new BigDecimal("0.015");

    @Override
    public Account process(Account account) {

        if (account.getBalance() == null) {
            throw new InvalidTransactionException(
                    "Cuenta " + account.getAccountId()
                            + " sin saldo"
            );
        }

        if (account.getAge() == null) {
            throw new InvalidTransactionException(
                    "Cuenta " + account.getAccountId()
                            + " sin edad"
            );
        }

        if (account.getAge() < 18 || account.getAge() > 100) {
            throw new InvalidTransactionException(
                    "Edad inválida en cuenta "
                            + account.getAccountId()
                            + ": " + account.getAge()
            );
        }

        if (account.getType() == null) {
            throw new InvalidTransactionException(
                    "Cuenta " + account.getAccountId()
                            + " sin tipo"
            );
        }

        BigDecimal rate;

        switch (account.getType().toLowerCase()) {

            case "ahorro":
                rate = SAVINGS_RATE;
                break;

            case "prestamo":
                rate = LOAN_RATE;
                break;

            case "hipoteca":
                rate = MORTGAGE_RATE;
                break;

            default:
                throw new InvalidTransactionException(
                        "Tipo de cuenta inválido: "
                                + account.getType()
                                + " en cuenta "
                                + account.getAccountId()
                );
        }

        BigDecimal interest = account.getBalance()
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal finalBalance = account.getBalance()
                .add(interest)
                .setScale(2, RoundingMode.HALF_UP);

        account.setInterestRate(rate);
        account.setInterestAmount(interest);
        account.setFinalBalance(finalBalance);

        log.info(
                "Cuenta {} | Tipo: {} | Saldo: {} | Tasa: {} | Interés: {} | Saldo final: {} | Hilo: {}",
                account.getAccountId(),
                account.getType(),
                account.getBalance(),
                rate,
                interest,
                finalBalance,
                Thread.currentThread().getName()
        );

        return account;
    }
}
