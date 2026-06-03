package com.vetsentinel.service;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Parasito;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.model.RiskLevel;
import com.vetsentinel.model.RiskResult;
import com.vetsentinel.model.RiskAssessmentResult;

import java.util.List;

public class RiskAssessmentService {

    private final List<RiskStrategy> strategies;

    public RiskAssessmentService(List<RiskStrategy> strategies) {
        this.strategies = strategies;
    }

    public RiskAssessmentResult evaluarRiesgoHumano(Diagnostico diagnostico) {
        Mascota mascota = diagnostico.getMascota();
        Propietario dueno = mascota.getPropietario();
        Parasito parasito = diagnostico.getParasito();

        StringBuilder alerta = new StringBuilder();
        alerta.append("=== ALERTA DE CONVIVENCIA - VetSentinel ===\n\n");
        alerta.append("Mascota: ").append(mascota.getNombre())
              .append(" (").append(mascota.getEspecie()).append(", ").append(mascota.getEdad()).append(" años)\n");
        alerta.append("Propietario: ").append(dueno.getNombre()).append("\n");
        alerta.append("Dirección: ").append(dueno.getDireccion()).append("\n");
        alerta.append("Parásito detectado: ").append(parasito.getNombre()).append("\n\n");

        RiskResult result = null;
        for (RiskStrategy strategy : strategies) {
            if (strategy.canHandle(diagnostico)) {
                result = strategy.evaluate(diagnostico);
                if (result != null) {
                    break;
                }
            }
        }

        RiskLevel nivelFinal = RiskLevel.MEDIO;
        if (result != null) {
            nivelFinal = result.getNivel();
            alerta.append("NIVEL: ").append(nivelFinal.getDbValue()).append("\n")
                  .append(result.getDetalle());
        } else {
            alerta.append("NIVEL: MEDIO\nConvivencia normal. Mantenga las medidas preventivas generales.\n\n");
        }

        alerta.append("ACCIONES RECOMENDADAS:\n").append(parasito.getMedidasPreventivas());
        return new RiskAssessmentResult(nivelFinal, alerta.toString());
    }
}
