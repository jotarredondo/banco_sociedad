package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.AnnualStatement;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AnnualStatementProcessor
        implements ItemProcessor<AnnualStatement, AnnualStatement> {

    @Override
    public AnnualStatement process(AnnualStatement statement) {

        if (statement.getFinalBalance() == null) {
            return null;
        }

        if (statement.getFinalBalance()
                .compareTo(BigDecimal.ZERO) < 0) {
            statement.setStatus("REVIEW");

            System.out.println(
                    "Estado anual requiere revisión: cuenta "
                            + statement.getAccountId());

        } else {
            statement.setStatus("OK");}

        return statement;
    }
}
