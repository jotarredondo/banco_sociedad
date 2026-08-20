package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.BatchJobListener;
import com.duoc.banco_sociedad.listener.BatchStepListener;
import com.duoc.banco_sociedad.listener.TransactionSkipListener;
import com.duoc.banco_sociedad.model.Account;
import com.duoc.banco_sociedad.model.AnnualStatement;
import com.duoc.banco_sociedad.policy.TransactionRetryPolicy;
import com.duoc.banco_sociedad.policy.TransactionSkipPolicy;
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
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.infrastructure.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.task.AsyncTaskExecutor;

import java.time.LocalDate;

@Configuration
public class BatchConfig {

/*    private final TransactionProcessor transactionProcessor;
    private final AccountInterestProcessor accountInterestProcessor;
    private final AnnualStatementProcessor annualStatementProcessor;
    private final AsyncTaskExecutor batchTaskExecutor;
    private final TransactionSkipPolicy transactionSkipPolicy;
    private final TransactionSkipListener transactionSkipListener;
    private final TransactionRetryPolicy transactionRetryPolicy;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;

    public BatchConfig(
            TransactionProcessor transactionProcessor,
            AccountInterestProcessor accountInterestProcessor,
            AnnualStatementProcessor annualStatementProcessor,
            AsyncTaskExecutor batchTaskExecutor,
            TransactionSkipPolicy transactionSkipPolicy,
            TransactionSkipListener transactionSkipListener,
            TransactionRetryPolicy transactionRetryPolicy,
            BatchJobListener batchJobListener,
            BatchStepListener batchStepListener) {

        this.transactionProcessor = transactionProcessor;
        this.accountInterestProcessor = accountInterestProcessor;
        this.annualStatementProcessor = annualStatementProcessor;
        this.batchTaskExecutor = batchTaskExecutor;
        this.transactionSkipPolicy = transactionSkipPolicy;
        this.transactionSkipListener = transactionSkipListener;
        this.transactionRetryPolicy = transactionRetryPolicy;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
    }*/
}
