package com.duoc.banco_sociedad.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class BatchJobListener implements JobExecutionListener {

    private static final Logger log =
            LoggerFactory.getLogger(BatchJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {

        log.info(
                "Iniciando Job: {} | ID ejecución: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info(
                "Finalizando Job: {} | Estado: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());
    }
}
