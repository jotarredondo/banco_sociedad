package com.duoc.banco_sociedad.processor;

import com.duoc.banco_sociedad.model.QuarterlyInterest;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

@Component
public class QuarterlyInterestProcessor
        implements ItemProcessor<QuarterlyInterest, QuarterlyInterest> {

    private final Set<String> registrosProcesados = new HashSet<>();

    @Override
    public QuarterlyInterest process(QuarterlyInterest account) {

        if (account.getSaldo() == null) {
            System.out.println("Registro descartado por saldo nulo. Cuenta: " + account.getCuentaId());
            return null;
        }

        if (account.getEdad() == null) {
            System.out.println("Registro descartado por edad nula. Cuenta: " + account.getCuentaId());
            return null;
        }

        if (account.getEdad() < 18 || account.getEdad() > 110) {
            System.out.println(
                    "Registro descartado por edad fuera de rango. Cuenta: "
                            + account.getCuentaId()
                            + " Edad: "
                            + account.getEdad());
            return null;
        }

        if (account.getNombre() == null || account.getNombre().isBlank()
                || account.getNombre().equalsIgnoreCase("unknown")) {
            System.out.println("Registro descartado por nombre inválido. Cuenta: " + account.getCuentaId());
            return null;
        }

        if (account.getTipo() == null || account.getTipo().isBlank()) {
            System.out.println("Registro descartado por tipo vacío. Cuenta: " + account.getCuentaId());
            return null;
        }

        String tipo = account.getTipo().trim().toLowerCase();

        String clave = account.getCuentaId()
                + "|" + account.getNombre().trim().toLowerCase()
                + "|" + account.getSaldo()
                + "|" + account.getEdad()
                + "|" + tipo;

        if (!registrosProcesados.add(clave)) {
            System.out.println("Registro duplicado descartado. Cuenta: " + account.getCuentaId());
            return null;
        }

        BigDecimal tasa;
        switch (tipo) {
            case "ahorro":
                tasa = new BigDecimal("0.01");
                break;

            case "prestamo":
                tasa = new BigDecimal("0.02");
                break;

            case "hipoteca":
                tasa = new BigDecimal("0.015");
                break;

            default:
                System.out.println("Registro descartado por tipo inválido. Cuenta: " + account.getCuentaId() + " Tipo: " + account.getTipo());
                return null;
        }

        BigDecimal interes = account.getSaldo()
                        .multiply(tasa)
                        .setScale(2, RoundingMode.HALF_UP);

        BigDecimal saldoFinal = account.getSaldo()
                        .add(interes)
                        .setScale(2, RoundingMode.HALF_UP);

        account.setTipo(tipo);
        account.setInteresCalculado(interes);
        account.setSaldoFinal(saldoFinal);
        return account;
    }
}
