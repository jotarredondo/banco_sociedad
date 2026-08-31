package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.BatchJobListener;
import com.duoc.banco_sociedad.listener.BatchStepListener;
import com.duoc.banco_sociedad.model.AnnualStatement;
import com.duoc.banco_sociedad.model.AnnualTransaction;
import com.duoc.banco_sociedad.policy.TransactionSkipPolicy;
import com.duoc.banco_sociedad.processor.AnnualStatementProcessor;
import com.duoc.banco_sociedad.processor.AnnualTransactionProcessor;

import com.duoc.banco_sociedad.repository.AnnualTransactionRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
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
import org.springframework.core.task.AsyncTaskExecutor;

import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class AnnualStatementJobConfig {

    private final AnnualTransactionProcessor annualTransactionProcessor;
    private final AsyncTaskExecutor batchTaskExecutor;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;
    private final TransactionSkipPolicy transactionSkipPolicy;
    private final AnnualStatementProcessor annualStatementProcessor;

    public AnnualStatementJobConfig(
            AnnualTransactionProcessor annualTransactionProcessor,
            AnnualStatementProcessor annualStatementProcessor,
            AsyncTaskExecutor batchTaskExecutor,
            BatchJobListener batchJobListener,
            BatchStepListener batchStepListener,
            TransactionSkipPolicy transactionSkipPolicy) {

        this.annualTransactionProcessor = annualTransactionProcessor;
        this.annualStatementProcessor = annualStatementProcessor;
        this.batchTaskExecutor = batchTaskExecutor;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
        this.transactionSkipPolicy = transactionSkipPolicy;
    }

    @Bean
    public SynchronizedItemStreamReader<AnnualTransaction>
    annualTransactionReader() {

        FlatFileItemReader<AnnualTransaction> delegate =
                new FlatFileItemReaderBuilder<AnnualTransaction>()
                        .name("annualTransactionReader")
                        .resource(
                                new ClassPathResource(
                                        "data/cuentas_anuales.csv"
                                )
                        )
                        .linesToSkip(1)
                        .delimited()
                        .names(
                                "cuenta_id",
                                "fecha",
                                "transaccion",
                                "monto",
                                "descripcion"
                        )
                        .fieldSetMapper(fieldSet -> {

                            AnnualTransaction transaction =
                                    new AnnualTransaction();

                            transaction.setAccountId(
                                    fieldSet.readLong("cuenta_id")
                            );

                            transaction.setDate(
                                    parseDate(
                                            fieldSet.readString("fecha"))
                            );

                            transaction.setTransactionType(
                                    fieldSet.readString("transaccion"));

                            String amount =
                                    fieldSet.readString("monto");

                            if (amount == null || amount.isBlank()) {

                                transaction.setAmount(null);

                            } else {

                                transaction.setAmount(
                                        new BigDecimal(amount));
                            }

                            transaction.setDescription(
                                    fieldSet.readString("descripcion"));

                            return transaction;
                        })
                        .build();

        return new SynchronizedItemStreamReaderBuilder<AnnualTransaction>()
                .delegate(delegate)
                .build();
    }

    @Bean
    @StepScope
    public ListItemReader<Long> annualAccountIdReader(
            AnnualTransactionRepository annualTransactionRepository) {

        return new ListItemReader<>(
                annualTransactionRepository.findDistinctAccountIds()
        );
    }

    @Bean
    public JpaItemWriter<AnnualTransaction> annualTransactionWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<AnnualTransaction>()
                .entityManagerFactory(entityManagerFactory)
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
    public Step loadAnnualTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaItemWriter<AnnualTransaction> annualTransactionWriter) {

        return new StepBuilder(
                "loadAnnualTransactionsStep",
                jobRepository
        )
                .<AnnualTransaction, AnnualTransaction>chunk(5)
                .reader(annualTransactionReader())
                .processor(annualTransactionProcessor)
                .writer(annualTransactionWriter)

                .faultTolerant()
                .skipPolicy(transactionSkipPolicy)

                .listener(batchStepListener)
                .taskExecutor(batchTaskExecutor)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Step generateAnnualStatementsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ListItemReader<Long> annualAccountIdReader,
            JpaItemWriter<AnnualStatement> annualStatementWriter) {

        return new StepBuilder(
                "generateAnnualStatementsStep",
                jobRepository
        )
                .<Long, AnnualStatement>chunk(5)
                .reader(annualAccountIdReader)
                .processor(annualStatementProcessor)
                .writer(annualStatementWriter)
                .listener(batchStepListener)
                .transactionManager(transactionManager)
                .build();
    }

    private LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        DateTimeFormatter[] formats = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };

        for (DateTimeFormatter format : formats) {

            try {
                return LocalDate.parse(value, format);

            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    @Bean
    public Job annualStatementJob(
            JobRepository jobRepository,
            Step loadAnnualTransactionsStep,
            Step generateAnnualStatementsStep) {

        return new JobBuilder(
                "annualStatementJob",
                jobRepository
        )
                .listener(batchJobListener)
                .start(loadAnnualTransactionsStep)
                .next(generateAnnualStatementsStep)
                .build();
    }
}