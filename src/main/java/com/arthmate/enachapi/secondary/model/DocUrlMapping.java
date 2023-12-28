package com.arthmate.enachapi.secondary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "doc_id_url_mapping")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocUrlMapping {

    @Field(name = "document_id")
    private String documentId;

    @Field(name = "document_url")
    private String documentUrl;

    @Field(name = "nach_request_id")
    private String nachRequestId;

    @Field(name = "loan_id")
    private String loanId;

    @Field(name = "created_by")
    @CreatedBy
    private String createdBy;

    @Field(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

}
