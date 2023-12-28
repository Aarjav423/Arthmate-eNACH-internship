package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.TransactionSuccessSmsProcessor;
import com.arthmate.enachapi.batch.writer.TransactionSuccessSmsWriter;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.model.enums.CollectionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class TransactionSuccessSmsBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final TransactionSuccessSmsProcessor processor;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Value("${txn.success.sms.batch.chunk}")
    private int chunk;

    private Query getQuery() {
        return Query.query(
                Criteria.where("txn_status").is("S")
                        .andOperator(new Criteria().orOperator(
                                Criteria.where("is_txn_success_sms_sent").isNull(),
                                Criteria.where("is_txn_success_sms_sent").is(false)
                        )));
    }

    @Bean
    @StepScope
    public MongoItemReader<NachTransactions> transactionSuccessSmsReader() {
        return new MongoItemReaderBuilder<NachTransactions>().name("transactionSuccessSmsReader")
                .template(mongoTemplate)
                .collection(CollectionNames.NACH_TRANSACTIONS.getName())
                .query(getQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(NachTransactions.class)
                .build();
    }

    @Bean
    @StepScope
    public TransactionSuccessSmsWriter transactionSuccessSmsWriter() {
        TransactionSuccessSmsWriter writer = new TransactionSuccessSmsWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Step transactionSuccessSmsStep() {
        return stepBuilderFactory.get("transactionSuccessSmsStep")
                .<NachTransactions, NachTransactions>chunk(chunk)
                .reader(transactionSuccessSmsReader())
                .processor(processor)
                .writer(transactionSuccessSmsWriter())
                .build();
    }

    @Bean
    public Job transactionSuccessSmsJob() {
        return jobBuilderFactory.get("transactionSuccessSmsJob")
                .start(transactionSuccessSmsStep())
                .build();
    }


}
