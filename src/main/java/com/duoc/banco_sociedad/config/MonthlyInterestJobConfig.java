package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.BatchJobListener;
import com.duoc.banco_sociedad.listener.BatchStepListener;
import com.duoc.banco_sociedad.model.Account;
import com.duoc.banco_sociedad.policy.TransactionSkipPolicy;
import com.duoc.banco_sociedad.processor.AccountInterestProcessor;
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

@Configuration
public class MonthlyInterestJobConfig {

    private final AccountInterestProcessor accountInterestProcessor;
    private final AsyncTaskExecutor batchTaskExecutor;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;
    private final TransactionSkipPolicy transactionSkipPolicy;

    public MonthlyInterestJobConfig(
            AccountInterestProcessor accountInterestProcessor,
            AsyncTaskExecutor batchTaskExecutor,
            BatchJobListener batchJobListener,
            BatchStepListener batchStepListener,
            TransactionSkipPolicy transactionSkipPolicy) {

        this.accountInterestProcessor = accountInterestProcessor;
        this.batchTaskExecutor = batchTaskExecutor;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
        this.transactionSkipPolicy = transactionSkipPolicy;
    }


    @Bean
    public SynchronizedItemStreamReader<Account> accountReader() {

        FlatFileItemReader<Account> delegate =
                new FlatFileItemReaderBuilder<Account>()
                        .name("accountReader")
                        .resource(
                                new ClassPathResource(
                                        "data/intereses.csv"
                                )
                        )
                        .linesToSkip(1)
                        .delimited()
                        .names(
                                "cuenta_id",
                                "nombre",
                                "saldo",
                                "edad",
                                "tipo"
                        )
                        .fieldSetMapper(fieldSet -> {

                            Account account = new Account();

                            account.setAccountId(
                                    fieldSet.readLong("cuenta_id")
                            );

                            account.setName(
                                    fieldSet.readString("nombre")
                            );

                            String balance =
                                    fieldSet.readString("saldo");

                            if (balance == null ||
                                    balance.isBlank()) {
                                account.setBalance(null);
                            } else {
                                account.setBalance(
                                        new BigDecimal(balance));
                            }

                            String age =
                                    fieldSet.readString("edad");

                            if (age == null ||
                                    age.isBlank()) {
                                account.setAge(null);
                            } else {
                                account.setAge(
                                        Integer.parseInt(age));
                            }

                            account.setType(
                                    fieldSet.readString("tipo"));

                            return account;
                        })
                        .build();

        return new SynchronizedItemStreamReaderBuilder<Account>()
                .delegate(delegate)
                .build();
    }

    @Bean
    public JpaItemWriter<Account> accountWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<Account>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step calculateMonthlyInterestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaItemWriter<Account> accountWriter) {

        return new StepBuilder("calculateMonthlyInterestStep", jobRepository)
                .<Account, Account>chunk(5)
                .reader(accountReader())
                .processor(accountInterestProcessor)
                .writer(accountWriter)

                .faultTolerant()
                .skipPolicy(transactionSkipPolicy)

                .listener(batchStepListener)
                .taskExecutor(batchTaskExecutor)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job monthlyInterestJob(
            JobRepository jobRepository,
            Step calculateMonthlyInterestStep) {

        return new JobBuilder("monthlyInterestJob", jobRepository)
                .listener(batchJobListener)
                .start(calculateMonthlyInterestStep)
                .build();
    }

}
