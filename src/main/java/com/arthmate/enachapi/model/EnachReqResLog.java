package com.arthmate.enachapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "enach_req_res_logs")
public class EnachReqResLog {

    @Field("_id")
    private ObjectId id;
    @Field("request_id")
    private String requestId;
    @Field("req_res_s3_url")
    private String reqResS3Url;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Field("created_by")
    @CreatedBy
    private String createdBy;

}
