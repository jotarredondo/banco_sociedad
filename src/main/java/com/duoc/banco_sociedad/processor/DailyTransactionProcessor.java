package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.DailyTransaction;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class DailyTransactionProcessor
        implements ItemProcessor<DailyTransaction, DailyTransaction> {

    @Override
    public DailyTransaction process(DailyTransaction transaction) {

        if (transaction.getMonto() == null) {
            System.out.println(
                    "Registro descartado por monto nulo. ID: "
                            + transaction.getId()
            );
            return null;
        }

        if (transaction.getFecha() == null) {
            System.out.println(
                    "Registro descartado por fecha inválida. ID: "
                            + transaction.getId()
            );
            return null;
        }

        if (transaction.getTipo() == null
                || transaction.getTipo().isBlank()) {

            System.out.println(
                    "Registro descartado por tipo vacío. ID: "
                            + transaction.getId()
            );
            return null;
        }

        String tipoNormalizado =
                transaction.getTipo().trim().toLowerCase();

        if (!tipoNormalizado.equals("credito")
                && !tipoNormalizado.equals("debito")) {

            System.out.println(
                    "Registro descartado por tipo inválido. ID: "
                            + transaction.getId()
                            + " Tipo: "
                            + transaction.getTipo()
            );

            return null;
        }

        transaction.setTipo(tipoNormalizado);

        if (transaction.getMonto().signum() < 0) {
            System.out.println(
                    "Transacción anómala detectada. ID: "
                            + transaction.getId()
                            + " Monto: "
                            + transaction.getMonto()
            );
        }

        return transaction;
    }
}
