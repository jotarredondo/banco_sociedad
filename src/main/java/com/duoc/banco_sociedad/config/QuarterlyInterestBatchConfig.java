package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.model.QuarterlyInterest;
import com.duoc.banco_sociedad.processor.QuarterlyInterestProcessor;
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

@Configuration
public class QuarterlyInterestBatchConfig {

    @Bean
    public FlatFileItemReader<QuarterlyInterest> accountReader() {

        return new FlatFileItemReaderBuilder<QuarterlyInterest>()
                .name("quarterlyInterestReader")
                .resource(new ClassPathResource("data/intereses_trimestrales.csv"))
                .linesToSkip(1)
                .delimited()
                .names("cuenta_id", "nombre", "saldo", "edad", "tipo")
                .fieldSetMapper(fieldSet -> {
                    QuarterlyInterest account = new QuarterlyInterest();
                    account.setCuentaId(
                            fieldSet.readLong("cuenta_id"));
                    account.setNombre(
                            fieldSet.readString("nombre"));
                    String saldo = fieldSet.readString("saldo");
                    account.setSaldo(
                            saldo == null || saldo.isBlank() ? null : new BigDecimal(saldo));
                    String edad = fieldSet.readString("edad");
                    account.setEdad(
                            edad == null || edad.isBlank() ? null : Integer.valueOf(edad));
                    account.setTipo(
                            fieldSet.readString("tipo"));
                    return account;
                })
                .build();
    }

    @Bean
    public JpaItemWriter<QuarterlyInterest> accountWriter(
            EntityManagerFactory entityManagerFactory) {

        return new JpaItemWriterBuilder<QuarterlyInterest>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step calculateQuarterlyInterestStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            QuarterlyInterestProcessor accountInterestProcessor,
            JpaItemWriter<QuarterlyInterest> accountWriter) {

        return new StepBuilder(
                "calculateQuarterlyInterestStep",
                jobRepository
        )
                .<QuarterlyInterest, QuarterlyInterest>chunk(10)
                .reader(accountReader())
                .processor(accountInterestProcessor)
                .writer(accountWriter)
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job quarterlyInterestJob(
            JobRepository jobRepository,
            Step calculateQuarterlyInterestStep) {

        return new JobBuilder("quarterlyInterestJob", jobRepository)
                .start(calculateQuarterlyInterestStep)
                .build();
    }
}
