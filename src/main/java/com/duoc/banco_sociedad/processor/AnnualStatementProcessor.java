package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.AnnualStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AnnualStatementProcessor implements ItemProcessor<AnnualStatement, AnnualStatement> {

    private static final Logger log = LoggerFactory.getLogger(AnnualStatementProcessor.class);

    @Override
    public AnnualStatement process(AnnualStatement statement) {

        if (statement.getFinalBalance() == null) {
            return null;
        }

        log.info(
                "Procesando estado anual {} en hilo {}",
                statement.getId(),
                Thread.currentThread().getName()
        );

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
