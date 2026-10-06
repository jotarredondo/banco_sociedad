package com.duoc.banco_sociedad.config;

import com.duoc.banco_sociedad.listener.AnnualStatementJobListener;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;

import static com.duoc.banco_sociedad.utils.ParseDate.parseDate;

@Configuration
public class AnnualStatementBatchConfig {

    @Bean
    public FlatFileItemReader<AnnualStatement> annualStatementReader() {

        return new FlatFileItemReaderBuilder<AnnualStatement>()
                .name("annualStatementReader")
                .resource(
                        new ClassPathResource(
                                "data/estados_financieros_anuales.csv"))
                .linesToSkip(1)
                .delimited()
                .names(
                        "cuenta_id",
                        "fecha",
                        "transaccion",
                        "monto",
                        "descripcion")
                .fieldSetMapper(fieldSet -> {
                    AnnualStatement statement =
                            new AnnualStatement();
                    statement.setCuentaId(
                            fieldSet.readLong("cuenta_id")
                    );
                    statement.setFecha(
                            parseDate(fieldSet.readString("fecha")));
                    statement.setTransaccion(
                            fieldSet.readString("transaccion"));
                    String monto =
                            fieldSet.readString("monto");
                    statement.setMonto(
                            monto == null || monto.isBlank() ? null : new BigDecimal(monto));
                    statement.setDescripcion(
                            fieldSet.readString("descripcion"));
                    return statement;
                })
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
                .faultTolerant()
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public Job annualStatementJob(
            JobRepository jobRepository,
            Step generateAnnualStatementStep,
            AnnualStatementJobListener listener) {

        return new JobBuilder("annualStatementJob", jobRepository)
                .listener(listener)
                .start(generateAnnualStatementStep)
                .build();
    }


}
