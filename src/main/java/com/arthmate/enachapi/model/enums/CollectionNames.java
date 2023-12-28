package com.arthmate.enachapi.model.enums;

public enum CollectionNames {
    ENACH_DETAILS("enach_details"),
    ENACH_DETAILS_HISTORY("enach_details_history"),
    SINGLE_DATA_TRANSLATION("single_data_translation"),
    NACH_TRANSACTIONS("nach_transactions");


    private final String name;

    CollectionNames(String name) {
        this.name = name;
    }

    public String getName(){ return  this.name; }
}