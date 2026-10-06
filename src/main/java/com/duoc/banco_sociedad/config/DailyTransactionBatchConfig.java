package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.DailyTransactionJobListener;
import com.duoc.banco_sociedad.model.DailyTransaction;
import com.duoc.banco_sociedad.processor.DailyTransactionProcessor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;

import static com.duoc.banco_sociedad.utils.ParseDate.parseDate;

@Configuration
public class DailyTransactionBatchConfig {

    @Bean
    public FlatFileItemReader<DailyTransaction> transactionReader() {

        return new FlatFileItemReaderBuilder<DailyTransaction>()
                .name("transactionReader")
                .resource(new ClassPathResource("data/movimientos_financieros_diarios.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .fieldSetMapper(fieldSet -> {
                    DailyTransaction transaction =
                            new DailyTransaction();
                    transaction.setId(
                            fieldSet.readLong("id"));
                    transaction.setFecha(
                            parseDate(fieldSet.readString("fecha")));
                    String monto =
                            fieldSet.readString("monto");
                    transaction.setMonto(
                            monto == null || monto.isBlank() ? null : new BigDecimal(monto));
                    transaction.setTipo(
                            fieldSet.readString("tipo"));
                    return transaction;
                })
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<DailyTransaction> synchronizedTransactionReader() {
        return new SynchronizedItemStreamReaderBuilder<DailyTransaction>()
                .delegate(transactionReader())
                .build();
    }

    @Bean
    public AsyncTaskExecutor transactionTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("transaction-thread-");
        executor.setConcurrencyLimit(3);
        return executor;
    }

    @Bean
    public JpaItemWriter<DailyTransaction> transactionWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<DailyTransaction>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step processTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            DailyTransactionProcessor transactionProcessor,
            JpaItemWriter<DailyTransaction> transactionWriter) {

        return new StepBuilder("processTransactionsStep", jobRepository)
                .<DailyTransaction, DailyTransaction>chunk(10)
                .reader(synchronizedTransactionReader())
                .processor(transactionProcessor)
                .writer(transactionWriter)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .taskExecutor(transactionTaskExecutor())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job dailyTransactionJob(
            JobRepository jobRepository,
            Step processTransactionsStep,
            DailyTransactionJobListener listener) {

        return new JobBuilder("dailyTransactionJob", jobRepository)
                .listener(listener)
                .start(processTransactionsStep)
                .build();
    }
}
