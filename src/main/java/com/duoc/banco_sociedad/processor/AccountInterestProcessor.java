package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.Account;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountInterestProcessor
        implements ItemProcessor<Account, Account> {

    @Override
    public Account process(Account account) {

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
