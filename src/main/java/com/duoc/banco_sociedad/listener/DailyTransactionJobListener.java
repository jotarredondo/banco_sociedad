package com.duoc.banco_sociedad.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class DailyTransactionJobListener implements JobExecutionListener {

    private final JobOperator jobOperator;

    public DailyTransactionJobListener(JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            System.out.println(
                    "Fallo crítico detectado en dailyTransactionJob. "
                            + "Se solicitará reejecución automática.");

            try {
                System.out.println("Iniciando reejecución automática...");
                jobOperator.restart(jobExecution);

            } catch (Exception e) {
                System.out.println("No fue posible reejecutar automáticamente el Job: " + e.getMessage());
            }
        }

        long readCount = 0;
        long writeCount = 0;
        long filterCount = 0;
        long skipCount = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            readCount += stepExecution.getReadCount();
            writeCount += stepExecution.getWriteCount();
            filterCount += stepExecution.getFilterCount();
            skipCount += stepExecution.getSkipCount();
        }

        System.out.println();
        System.out.println("===== RESUMEN TRANSACCIONES DIARIAS =====");
        System.out.println("Registros leídos: " + readCount);
        System.out.println("Registros persistidos: " + writeCount);
        System.out.println("Registros filtrados: " + filterCount);
        System.out.println("Registros omitidos por skip: " + skipCount);
        System.out.println("Estado Job: " + jobExecution.getStatus());
        System.out.println("==========================================");
        System.out.println();
    }
}
