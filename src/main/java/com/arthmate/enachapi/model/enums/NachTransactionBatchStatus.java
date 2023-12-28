package com.arthmate.enachapi.model.enums;

public enum NachTransactionBatchStatus {
    NEW("NEW"),

    PROCESSED("PROCESSED");

    final String label;

    private NachTransactionBatchStatus(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}
