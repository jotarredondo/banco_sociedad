package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.model.Account;
import com.duoc.banco_sociedad.model.AnnualStatement;
import com.duoc.banco_sociedad.model.Transaction;
import com.duoc.banco_sociedad.processor.AccountInterestProcessor;
import com.duoc.banco_sociedad.processor.AnnualStatementProcessor;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Objects;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<Transaction> transactionReader() {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionReader")
                .resource(new ClassPathResource("data/transactions.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "accountId", "amount", "type", "date")
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();

                    transaction.setId(
                            fieldSet.readLong("id"));
                    transaction.setAccountId(
                            fieldSet.readLong("accountId"));
                    transaction.setAmount(
                            fieldSet.readBigDecimal("amount"));
                    transaction.setType(
                            fieldSet.readString("type"));
                    transaction.setDate(
                            LocalDate.parse(Objects.requireNonNull(fieldSet.readString("date"))));
                    return transaction;
                })
                .build();
    }

    @Bean
    public JpaItemWriter<Transaction> transactionWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<Transaction>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public FlatFileItemReader<Account> accountReader() {

        return new FlatFileItemReaderBuilder<Account>()
                .name("accountReader")
                .resource(new ClassPathResource("data/accounts.csv"))
                .linesToSkip(1)
                .delimited()
                .names("id", "type", "balance", "interestRate")
                .targetType(Account.class)
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
    public FlatFileItemReader<AnnualStatement> annualStatementReader() {

        return new FlatFileItemReaderBuilder<AnnualStatement>()
                .name("annualStatementReader")
                .resource(
                        new ClassPathResource(
                                "data/annual_statements.csv"
                        )
                )
                .linesToSkip(1)
                .delimited()
                .names(
                        "id",
                        "accountId",
                        "year",
                        "totalDeposits",
                        "totalWithdrawals",
                        "finalBalance",
                        "status"
                )
                .targetType(AnnualStatement.class)
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
            AnnualStatementProcessor annualStatementProcessor,
            JpaItemWriter<AnnualStatement> annualStatementWriter) {

        return new StepBuilder(
                "generateAnnualStatementStep",
                jobRepository
        )
                .<AnnualStatement, AnnualStatement>chunk(10)
                .reader(annualStatementReader())
                .processor(annualStatementProcessor)
                .writer(annualStatementWriter)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Step processTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TransactionProcessor transactionProcessor,
            JpaItemWriter<Transaction> transactionWriter) {

        return new StepBuilder("processTransactionsStep", jobRepository)
                .<Transaction, Transaction>chunk(10)
                .reader(transactionReader())
                .processor(transactionProcessor)
                .writer(transactionWriter)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Step calculateMonthlyInterestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AccountInterestProcessor accountInterestProcessor,
            JpaItemWriter<Account> accountWriter) {

        return new StepBuilder(
                "calculateMonthlyInterestStep",
                jobRepository
        )
                .<Account, Account>chunk(10)
                .reader(accountReader())
                .processor(accountInterestProcessor)
                .writer(accountWriter)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job dailyTransactionJob(
            JobRepository jobRepository,
            Step processTransactionsStep) {

        return new JobBuilder("dailyTransactionJob", jobRepository)
                .start(processTransactionsStep)
                .build();
    }

    @Bean
    public Job monthlyInterestJob(
            JobRepository jobRepository,
            Step calculateMonthlyInterestStep) {

        return new JobBuilder(
                "monthlyInterestJob",
                jobRepository
        )
                .start(calculateMonthlyInterestStep)
                .build();
    }

    @Bean
    public Job annualStatementJob(
            JobRepository jobRepository,
            Step generateAnnualStatementStep) {

        return new JobBuilder(
                "annualStatementJob",
                jobRepository
        )
                .start(generateAnnualStatementStep)
                .build();
    }
}
