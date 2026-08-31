package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.BatchJobListener;
import com.duoc.banco_sociedad.listener.BatchStepListener;
import com.duoc.banco_sociedad.listener.DailyTransactionSummaryListener;
import com.duoc.banco_sociedad.listener.TransactionSkipListener;
import com.duoc.banco_sociedad.model.Transaction;
import com.duoc.banco_sociedad.policy.TransactionRetryPolicy;
import com.duoc.banco_sociedad.policy.TransactionSkipPolicy;
import com.duoc.banco_sociedad.processor.TransactionProcessor;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Configuration
public class DailyTransactionJobConfig {

    private final TransactionProcessor transactionProcessor;
    private final AsyncTaskExecutor batchTaskExecutor;
    private final TransactionSkipPolicy transactionSkipPolicy;
    private final TransactionSkipListener transactionSkipListener;
    private final TransactionRetryPolicy transactionRetryPolicy;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;
    private final DailyTransactionSummaryListener dailyTransactionSummaryListener;

    public DailyTransactionJobConfig(
            TransactionProcessor transactionProcessor,
            AsyncTaskExecutor batchTaskExecutor,
            TransactionSkipPolicy transactionSkipPolicy,
            TransactionSkipListener transactionSkipListener,
            TransactionRetryPolicy transactionRetryPolicy,
            BatchJobListener batchJobListener,
            BatchStepListener batchStepListener,
            DailyTransactionSummaryListener dailyTransactionSummaryListener) {


        this.transactionProcessor = transactionProcessor;
        this.batchTaskExecutor = batchTaskExecutor;
        this.transactionSkipPolicy = transactionSkipPolicy;
        this.transactionSkipListener = transactionSkipListener;
        this.transactionRetryPolicy = transactionRetryPolicy;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
        this.dailyTransactionSummaryListener = dailyTransactionSummaryListener;
    }

    @Bean
    public SynchronizedItemStreamReader<Transaction> transactionReader() {

        FlatFileItemReader<Transaction> delegate =
                new FlatFileItemReaderBuilder<Transaction>()
                        .name("transactionReader")
                        .resource(new ClassPathResource("data/transacciones.csv"))
                        .linesToSkip(1)
                        .delimited()
                        .names("id", "fecha", "monto", "tipo")
                        .fieldSetMapper(fieldSet -> {

                            Transaction transaction = new Transaction();

                            transaction.setId(fieldSet.readLong("id"));
                            transaction.setType(fieldSet.readString("tipo"));

                            String amountValue = fieldSet.readString("monto");

                            if (amountValue == null || amountValue.isBlank()) {
                                transaction.setAmount(null);
                            } else {
                                transaction.setAmount(
                                        new BigDecimal(amountValue));
                            }
                            transaction.setDate(parseDate(fieldSet.readString("fecha")));

                            return transaction;
                        })
                        .build();

        return new SynchronizedItemStreamReaderBuilder<Transaction>()
                .delegate(delegate)
                .build();
    }

    private LocalDate parseDate(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        return null;
    }

    @Bean
    public JpaItemWriter<Transaction> transactionWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<Transaction>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step processTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaItemWriter<Transaction> transactionWriter) {

        return new StepBuilder("processTransactionsStep", jobRepository)
                .<Transaction, Transaction>chunk(5)
                .reader(transactionReader())
                .processor(transactionProcessor)
                .writer(transactionWriter)

                .faultTolerant()
                .skipPolicy(transactionSkipPolicy)
                .retryPolicy(transactionRetryPolicy.retryPolicy())
                .listener(transactionSkipListener)
                .listener(batchStepListener)

                .taskExecutor(batchTaskExecutor)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job dailyTransactionJob(
            JobRepository jobRepository,
            Step processTransactionsStep) {

        return new JobBuilder("dailyTransactionJob", jobRepository)
                .listener(batchJobListener)
                .listener(dailyTransactionSummaryListener)
                .start(processTransactionsStep)
                .build();
    }
}
