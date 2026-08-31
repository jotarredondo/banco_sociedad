package com.duoc.banco_sociedad.listener;

import com.duoc.banco_sociedad.model.DailyTransactionSummary;
import com.duoc.banco_sociedad.repository.DailyTransactionSummaryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DailyTransactionSummaryListener
        implements JobExecutionListener {

    private static final Logger log =
            LoggerFactory.getLogger(DailyTransactionSummaryListener.class);

    private final DailyTransactionSummaryRepository summaryRepository;

    public DailyTransactionSummaryListener(
            DailyTransactionSummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Preparando resumen de transacciones diarias");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        long totalRead = 0;
        long totalWritten = 0;
        long totalSkipped = 0;

        for (StepExecution stepExecution :
                jobExecution.getStepExecutions()) {

            totalRead += stepExecution.getReadCount();
            totalWritten += stepExecution.getWriteCount();
            totalSkipped += stepExecution.getSkipCount();
        }

        DailyTransactionSummary summary =
                new DailyTransactionSummary();

        summary.setExecutionDate(LocalDateTime.now());

        summary.setTotalRead(totalRead);
        summary.setTotalWritten(totalWritten);
        summary.setTotalSkipped(totalSkipped);

        // Estos campos los completaremos después si es necesario.
        summary.setTotalCredits(0);
        summary.setTotalDebits(0);
        summary.setTotalCreditAmount(BigDecimal.ZERO);
        summary.setTotalDebitAmount(BigDecimal.ZERO);

        summary.setStatus(jobExecution.getStatus().toString());
        summary.setExecutionTimeMs(0);

        log.info("Guardando resumen diario...");

        summaryRepository.save(summary);

        log.info("==========================================");
        log.info("RESUMEN DE TRANSACCIONES DIARIAS");
        log.info("Total leídos: {}", totalRead);
        log.info("Total válidos escritos: {}", totalWritten);
        log.info("Total inválidos omitidos: {}", totalSkipped);
        log.info("Estado: {}", jobExecution.getStatus());
        log.info("==========================================");
    }
}