package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.AnnualStatement;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class AnnualStatementProcessor
        implements ItemProcessor<AnnualStatement, AnnualStatement> {

    @Override
    public AnnualStatement process(
            AnnualStatement statement) {

        if (statement.getFecha() == null) {
            System.out.println("Registro descartado por fecha inválida. Cuenta: " + statement.getCuentaId());
            return null;
        }

        if (statement.getMonto() == null) {
            System.out.println("Registro descartado por monto nulo. Cuenta: " + statement.getCuentaId());
            return null;
        }

        if (statement.getTransaccion() == null || statement.getTransaccion().isBlank()) {
            System.out.println("Registro descartado por transacción vacía. Cuenta: " + statement.getCuentaId());
            return null;
        }

        String transaccionNormalizada = statement.getTransaccion().trim().toLowerCase();

        if (transaccionNormalizada.equals("depósito")) {
            transaccionNormalizada = "deposito";
        }

        if (!transaccionNormalizada.equals("compra") && !transaccionNormalizada.equals("deposito") && !transaccionNormalizada.equals("retiro")&& !transaccionNormalizada.equals("pago")) {
            System.out.println(
                    "Registro descartado por transacción inválida. Cuenta: " + statement.getCuentaId() + " Tipo: " + statement.getTransaccion());
            return null;
        }
        statement.setTransaccion(transaccionNormalizada);

        if (statement.getDescripcion() == null || statement.getDescripcion().isBlank()) {
            statement.setDescripcion(
                    "SIN DESCRIPCION");
        }
        return statement;
    }
}
