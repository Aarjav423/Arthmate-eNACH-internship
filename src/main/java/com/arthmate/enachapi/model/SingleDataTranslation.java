package com.arthmate.enachapi.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "single_data_translation")
@Data
public class SingleDataTranslation {

    @Field(name = "_id")
    private ObjectId id;

    private String type;
    private String key;
    private String value;
}
