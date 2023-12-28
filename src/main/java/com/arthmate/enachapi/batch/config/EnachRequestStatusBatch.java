package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.EnachRequestStatusProcessor;
import com.arthmate.enachapi.batch.writer.EnachRequestStatusWriter;
import com.arthmate.enachapi.dto.EnachStatusResponseDto;
import com.arthmate.enachapi.model.EnachDetail;
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
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EnachRequestStatusBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final EnachRequestStatusProcessor enachRequestStatusProcessor;

    @Value("${enach.request.status.chunk}")
    private int chunkSize;
    @Value("${enach.status.batch.after.minutes}")
    private long runStatusBatchAfterMins;
    @Value("${enach.status.batch.check.till.days}")
    private long checkStatusTillDays;
    @Value("${enach.request.status.check}")
    private List<String> statusesToCheck;

    private Query getEnachRequestStatusQuery() {
        return Query.query(
                Criteria.where("status").in(statusesToCheck)
                        .and("updated_at").lte(LocalDateTime.now().minusMinutes(runStatusBatchAfterMins))
                        .and("created_at").gte(LocalDateTime.now().minusDays(checkStatusTillDays))
        );
    }

    @Bean
    @StepScope
    public MongoItemReader<EnachDetail> enachRequestStatusReader() {
        return new MongoItemReaderBuilder<EnachDetail>().name("enachRequestStatusReader")
                .template(mongoTemplate)
                .collection(CollectionNames.ENACH_DETAILS.getName())
                .query(getEnachRequestStatusQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(EnachDetail.class)
                .build();
    }

    @Bean
    @StepScope
    public EnachRequestStatusWriter enachRequestStatusWriter() {
        EnachRequestStatusWriter writer = new EnachRequestStatusWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Step enachRequestStatusStep() {
        return stepBuilderFactory.get("enachRequestStatusStep")
                .<EnachDetail, EnachStatusResponseDto>chunk(chunkSize)
                .reader(enachRequestStatusReader())
                .processor(enachRequestStatusProcessor)
                .writer(enachRequestStatusWriter())
                .build();
    }

    @Bean
    public Job enachRequestStatusJob() {
        return jobBuilderFactory.get("enachRequestStatusJob")
                .start(enachRequestStatusStep())
                .build();
    }
}
