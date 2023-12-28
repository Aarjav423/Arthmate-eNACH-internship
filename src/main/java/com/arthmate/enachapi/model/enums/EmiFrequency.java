package com.arthmate.enachapi.model.enums;

public enum EmiFrequency {
    MONTHLY("MONTHLY"),
    QUARTERLY("QUARTERLY"),
    HALF_YEARLY("HALF_YEARLY");

    final String label;

    private EmiFrequency(String label) {
        this.label = label;
    }

    public String label(){
        return this.label;
    }
}
