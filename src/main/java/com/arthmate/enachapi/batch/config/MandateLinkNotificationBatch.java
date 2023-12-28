package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.processor.MandateLinkNotificationProcessor;
import com.arthmate.enachapi.batch.writer.MandateLinkNotificationWriter;
import com.arthmate.enachapi.dto.MandateLinkNotificationDto;
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

import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class MandateLinkNotificationBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final MandateLinkNotificationProcessor processor;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Value("${mandate.link.notification.retry.limit}")
    private int retryLimit;
    @Value("${mandate.link.notification.chunk}")
    private int chunk;

    private Query getQuery() {
        Criteria smsFilter = new Criteria().andOperator(
                Criteria.where("is_sms_required").is(true),
                new Criteria().orOperator(
                        Criteria.where("is_sms_sent").isNull(),
                        Criteria.where("is_sms_sent").is(false)
                )
        );

        Criteria emailFilter = new Criteria().andOperator(
                Criteria.where("is_email_required").is(true),
                new Criteria().orOperator(
                        Criteria.where("is_email_sent").isNull(),
                        Criteria.where("is_email_sent").is(false)
                )
        );

        Criteria mandateInitiatedFilter = new Criteria().orOperator(
                Criteria.where("is_mandate_initiated_sms_sent").isNull(),
                Criteria.where("is_mandate_initiated_sms_sent").is(false)
        );

        Criteria retryFilter = new Criteria().orOperator(
                Criteria.where("mandate_link_notification_retries").isNull(),
                Criteria.where("mandate_link_notification_retries").lt(retryLimit)
        );

        return Query.query(
                new Criteria().andOperator(retryFilter,
                        new Criteria().orOperator(emailFilter, smsFilter, mandateInitiatedFilter))
        );
    }

    @Bean
    @StepScope
    public MongoItemReader<EnachDetail> mandateLinkNotificationReader() {
        return new MongoItemReaderBuilder<EnachDetail>().name("linkNotificationReader")
                .template(mongoTemplate)
                .collection(CollectionNames.ENACH_DETAILS.getName())
                .query(getQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(EnachDetail.class)
                .build();
    }

    @Bean
    @StepScope
    public MandateLinkNotificationWriter mandateLinkNotificationWriter() {
        MandateLinkNotificationWriter writer = new MandateLinkNotificationWriter();
        writer.setTemplate(mongoTemplate);
        return writer;
    }

    @Bean
    public Step mandateLinkNotificationStep() {
        return stepBuilderFactory.get("mandateLinkNotificationStep")
                .<EnachDetail, MandateLinkNotificationDto>chunk(chunk)
                .reader(mandateLinkNotificationReader())
                .processor(processor)
                .writer(mandateLinkNotificationWriter())
                .build();
    }

    @Bean
    public Job mandateLinkNotificationJob() {
        return jobBuilderFactory.get("mandateLinkNotificationJob")
                .start(mandateLinkNotificationStep())
                .build();
    }

}
