package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.EnachUpdateStatusProcessor;
import com.arthmate.enachapi.batch.writer.EnachUpdateStatusWriter;
import com.arthmate.enachapi.dto.EnachUpdateStatusDto;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.enums.CollectionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EnachUpdateStatusBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final EnachUpdateStatusProcessor enachUpdateStatusProcessor;
    private final JobBuilderFactory jobBuilderFactory;

    @Value("${enach.request.status.chunk}")
    private int chunkSize;

    @Value("${enach.update.status.string-to-match}")
    private String[] statusesToMatch;

    @Value("${enach.update.status.after.minutes}")
    private long updateStatusAfterMins;

    private Query getEnachUpdateStatusQuery() {
        return Query.query(
                Criteria.where("status").in(statusesToMatch)
                        .and("updated_at").lte(LocalDateTime.now().minusMinutes(updateStatusAfterMins))
        );
    }

    @Bean
    @StepScope
    public MongoItemReader<EnachDetail> enachUpdateStatusReader() {
        return new MongoItemReaderBuilder<EnachDetail>().name("enachUpdateStatusReader")
                .template(mongoTemplate)
                .collection(CollectionNames.ENACH_DETAILS.getName())
                .query(getEnachUpdateStatusQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(EnachDetail.class)
                .build();
    }

    @Bean
    public Step enachUpdateStatusStep() {
        return stepBuilderFactory.get("enachUpdateStatusStep")
                .<EnachDetail, EnachUpdateStatusDto>chunk(chunkSize)
                .reader(enachUpdateStatusReader())
                .processor(enachUpdateStatusProcessor)
                .writer(enachUpdateStatusWriter())
                .build();
    }

    @Bean
    @StepScope
    public EnachUpdateStatusWriter enachUpdateStatusWriter() {
        EnachUpdateStatusWriter writer = new EnachUpdateStatusWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Job enachUpdateStatusJob() {
        return jobBuilderFactory.get("enachUpdateStatusJob")
                .start(enachUpdateStatusStep())
                .build();
    }
}
