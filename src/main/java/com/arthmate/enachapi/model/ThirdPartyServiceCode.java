package com.arthmate.enachapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("third_party_service_codes")
public class ThirdPartyServiceCode {

    @Field("_id")
    private ObjectId id;
    @Field("service")
    private String service;
    @Field("service_code_id")
    private String serviceCodeId;
    @Field("service_short_code")
    private String serviceShortCode;
    @Field("code_category")
    private String codeCategory;
    @Field("code_subcategory")
    private String codeSubcategory;
    @Field("code_id")
    private String codeId;
    @Field("code_desc")
    private String codeDesc;

}
