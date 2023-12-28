package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.ScheduleNachTransactionProcessor;
import com.arthmate.enachapi.batch.writer.ScheduleNachTransactionWriter;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.model.enums.CollectionNames;
import com.arthmate.enachapi.model.enums.NachTransactionBatchStatus;
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

import java.time.LocalDate;
import java.util.Map;

import static com.arthmate.enachapi.model.enums.NachTransactionBatchStatus.NEW;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ScheduleNachTransactionBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final ScheduleNachTransactionProcessor processor;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Value("${schedule.nach.transaction.chunk}")
    private int chunk;

    private Query getQuery() {
        return Query.query(
                Criteria.where("status").is(NEW.label())
                        .and("scheduled_on").gte(LocalDate.now().atStartOfDay())
        );
    }

    @Bean
    @StepScope
    public MongoItemReader<NachTransactions> scheduleNachTransactionReader() {
        return new MongoItemReaderBuilder<NachTransactions>().name("scheduleNachTransactionReader")
                .template(mongoTemplate)
                .collection(CollectionNames.NACH_TRANSACTIONS.getName())
                .query(getQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(NachTransactions.class)
                .build();
    }

    @Bean
    @StepScope
    public ScheduleNachTransactionWriter scheduleNachTransactionWriter() {
        ScheduleNachTransactionWriter writer = new ScheduleNachTransactionWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Step scheduleNachTransactionStep() {
        return stepBuilderFactory.get("scheduleNachTransactionStep")
                .<NachTransactions, NachTransactions>chunk(chunk)
                .reader(scheduleNachTransactionReader())
                .processor(processor)
                .writer(scheduleNachTransactionWriter())
                .build();
    }

    @Bean
    public Job scheduleNachTransactionJob() {
        return jobBuilderFactory.get("scheduleNachTransactionJob")
                .start(scheduleNachTransactionStep())
                .build();
    }

}
