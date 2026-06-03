package com.vetsentinel.model;

public class RiskAssessmentResult {
    private final RiskLevel nivel;
    private final String alertaTexto;

    public RiskAssessmentResult(RiskLevel nivel, String alertaTexto) {
        this.nivel = nivel;
        this.alertaTexto = alertaTexto;
    }

    public RiskLevel getNivel() {
        return nivel;
    }

    public String getAlertaTexto() {
        return alertaTexto;
    }
}
