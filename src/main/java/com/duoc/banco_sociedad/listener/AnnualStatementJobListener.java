package com.duoc.banco_sociedad.listener;

import com.duoc.banco_sociedad.repository.AnnualStatementRepository;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AnnualStatementJobListener
        implements JobExecutionListener {

    private final AnnualStatementRepository annualStatementRepository;

    public AnnualStatementJobListener(
            AnnualStatementRepository annualStatementRepository) {
        this.annualStatementRepository = annualStatementRepository;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        List<Object[]> resultados = annualStatementRepository.obtenerResumenAnual();
        System.out.println();
        System.out.println("===== RESUMEN ESTADOS DE CUENTA ANUALES =====");

        for (Object[] fila : resultados) {
            Long cuentaId = ((Number) fila[0]).longValue();
            Integer year = ((Number) fila[1]).intValue();
            BigDecimal totalDepositos = new BigDecimal(fila[2].toString());
            BigDecimal totalEgresos = new BigDecimal(fila[3].toString());
            Integer cantidadMovimientos = ((Number) fila[4]).intValue();
            BigDecimal saldoFinal = totalDepositos.subtract(totalEgresos);
            System.out.println("Cuenta: " + cuentaId
                            + " | Año: " + year
                            + " | Depósitos: " + totalDepositos
                            + " | Egresos: " + totalEgresos
                            + " | Saldo: " + saldoFinal
                            + " | Movimientos: " + cantidadMovimientos
            );
        }
        System.out.println("Total de estados anuales generados: " + resultados.size());
        System.out.println("============================================");
    }
}