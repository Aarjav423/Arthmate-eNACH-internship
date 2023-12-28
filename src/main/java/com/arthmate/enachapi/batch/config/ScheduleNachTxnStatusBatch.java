package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.ScheduleNachTxnStatusProcessor;
import com.arthmate.enachapi.batch.writer.ScheduleNachTxnStatusWriter;
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

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ScheduleNachTxnStatusBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final ScheduleNachTxnStatusProcessor processor;

    @Value("${schedule.nach.transaction.status.chunk}")
    private int chunkSize;
    @Value("${schedule.nach.transaction.status.check.days}")
    private long checkForDays; // status will be checked till #days of created_at

    private Query getQuery() {
        return Query.query(Criteria.where("txn_status").is("I")
                .and("created_at").gt(LocalDateTime.now().minusDays(checkForDays)));
    }

    @Bean
    @StepScope
    MongoItemReader<NachTransactions> scheduleNachTxnStatusReader() {
        return new MongoItemReaderBuilder<NachTransactions>()
                .name("scheduleNachTxnStatusReader")
                .template(mongoTemplate)
                .collection(CollectionNames.NACH_TRANSACTIONS.getName())
                .query(getQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(NachTransactions.class)
                .build();
    }

    @Bean
    @StepScope
    ScheduleNachTxnStatusWriter scheduleNachTxnStatusWriter() {
        ScheduleNachTxnStatusWriter writer = new ScheduleNachTxnStatusWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Step scheduleNachTxnStatusStep() {
        return stepBuilderFactory.get("scheduleNachTxnStatusStep")
                .<NachTransactions, NachTransactions>chunk(chunkSize)
                .reader(scheduleNachTxnStatusReader())
                .processor(processor)
                .writer(scheduleNachTxnStatusWriter())
                .build();
    }

    @Bean
    public Job scheduleNachTxnStatusJob() {
        return jobBuilderFactory.get("scheduleNachTxnStatusJob")
                .start(scheduleNachTxnStatusStep())
                .build();
    }

}
