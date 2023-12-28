package com.arthmate.enachapi.batch.config;

import com.arthmate.enachapi.batch.writer.EnachBICUpdateWriter;
import com.arthmate.enachapi.dto.BICReqDto;
import com.arthmate.enachapi.model.enums.CollectionNames;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EnachBICUpdateBatch {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Value("${bic.enach.status.update.chunk}")
    private int chunkSize;

    private Query getEnachBICUpdateStatusQuery() {
        Query query = new Query().addCriteria( Criteria.where("status_notified").is(0)
        );
        query.fields().include("request_id",
                "external_ref_num","corporate_name","purpose_of_mandate","customer_mobile_no",
                "customer_name","start_date","mandate_id","created_at","end_date","status_desc","account_no","amount","status","_id");
        return query;
    }

    @Bean
    @StepScope
    public MongoItemReader<BICReqDto> enachBICUpdateStatusReader() {
        return new MongoItemReaderBuilder<BICReqDto>()
                .name("enachBICUpdateStatusReader")
                .template(mongoTemplate)
                .collection(CollectionNames.ENACH_DETAILS.getName())
                .query(getEnachBICUpdateStatusQuery())
                .sorts(Map.of("_id", Sort.Direction.ASC))
                .targetType(BICReqDto.class)
                .build();
    }

    @Bean
    public Step enachBICUpdateStatusStep() {
        return stepBuilderFactory.get("enachBICUpdateStatusStep")
                .<BICReqDto, BICReqDto>chunk(chunkSize)
                .reader(enachBICUpdateStatusReader())
                .writer(enachBICUpdateStatusWriter())
                .build();
    }

    @Bean
    @StepScope
    public EnachBICUpdateWriter enachBICUpdateStatusWriter() {
        return new EnachBICUpdateWriter();
    }

    @Bean
    public Job enachBICUpdateStatusJob() {
        return jobBuilderFactory.get("enachBICUpdateStatusJob")
                .start(enachBICUpdateStatusStep())
                .build();
    }
}
