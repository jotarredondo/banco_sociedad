package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountInterestProcessor implements ItemProcessor<Account, Account> {

    private static final Logger log =
            LoggerFactory.getLogger(AccountInterestProcessor.class);

    @Override
    public Account process(Account account) {

        log.info(
                "Procesando cuenta {} en hilo {}",
                account.getId(),
                Thread.currentThread().getName());

        if (account.getBalance() == null ||
                account.getInterestRate() == null) {
            return null;
        }

        BigDecimal interest = account.getBalance()
                .multiply(account.getInterestRate());

        if ("SAVINGS".equalsIgnoreCase(account.getType())) {
            account.setBalance(account.getBalance().add(interest));
        } else if ("LOAN".equalsIgnoreCase(account.getType())) {
            account.setBalance(account.getBalance().add(interest));
        }

        return account;
    }
}
