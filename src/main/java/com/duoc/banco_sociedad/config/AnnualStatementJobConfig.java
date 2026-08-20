package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.BatchJobListener;
import com.duoc.banco_sociedad.listener.BatchStepListener;
import com.duoc.banco_sociedad.model.AnnualStatement;
import com.duoc.banco_sociedad.processor.AnnualStatementProcessor;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.io.ClassPathResource;

import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.core.task.AsyncTaskExecutor;

@Configuration
public class AnnualStatementJobConfig {

    private final AnnualStatementProcessor annualStatementProcessor;
    private final AsyncTaskExecutor batchTaskExecutor;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;

    public AnnualStatementJobConfig(
            AnnualStatementProcessor annualStatementProcessor,
            AsyncTaskExecutor batchTaskExecutor,
            BatchJobListener batchJobListener,
            BatchStepListener batchStepListener) {

        this.annualStatementProcessor = annualStatementProcessor;
        this.batchTaskExecutor = batchTaskExecutor;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
    }

    @Bean
    public SynchronizedItemStreamReader<AnnualStatement> annualStatementReader() {

        FlatFileItemReader<AnnualStatement> delegate =
                new FlatFileItemReaderBuilder<AnnualStatement>()
                        .name("annualStatementReader")
                        .resource(new ClassPathResource("data/annual_statements.csv"))
                        .linesToSkip(1)
                        .delimited()
                        .names(
                                "id",
                                "accountId",
                                "year_",
                                "totalDeposits",
                                "totalWithdrawals",
                                "finalBalance",
                                "status"
                        )
                        .targetType(AnnualStatement.class)
                        .build();

        return new SynchronizedItemStreamReaderBuilder<AnnualStatement>()
                .delegate(delegate)
                .build();
    }

    @Bean
    public JpaItemWriter<AnnualStatement> annualStatementWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<AnnualStatement>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step generateAnnualStatementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaItemWriter<AnnualStatement> annualStatementWriter) {

        return new StepBuilder("generateAnnualStatementStep", jobRepository)
                .<AnnualStatement, AnnualStatement>chunk(5)
                .reader(annualStatementReader())
                .processor(annualStatementProcessor)
                .writer(annualStatementWriter)
                .listener(batchStepListener)
                .taskExecutor(batchTaskExecutor)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job annualStatementJob(
            JobRepository jobRepository,
            Step generateAnnualStatementStep) {

        return new JobBuilder("annualStatementJob", jobRepository)
                .listener(batchJobListener)
                .start(generateAnnualStatementStep)
                .build();
    }
}