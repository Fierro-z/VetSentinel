package com.vetsentinel.model;

public enum RiskLevel {
    EMERGENCIA_CRITICA("EMERGENCIA CRÍTICA"),
    CRITICO("CRITICO"),
    ALTO("ALTO"),
    MEDIO("MEDIO"),
    BAJO("BAJO");

    private final String dbValue;

    RiskLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }
}
