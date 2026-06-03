package com.vetsentinel.model;

public class RiskResult {
    private final RiskLevel nivel;
    private final String detalle;

    public RiskResult(RiskLevel nivel, String detalle) {
        this.nivel = nivel;
        this.detalle = detalle;
    }

    public RiskLevel getNivel() {
        return nivel;
    }

    public String getDetalle() {
        return detalle;
    }
}
