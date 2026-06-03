package com.vetsentinel.service;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Parasito;
import com.vetsentinel.model.Propietario;

import java.util.List;

public class RiskAssessmentService {

    private final List<RiskStrategy> strategies;

    public RiskAssessmentService(List<RiskStrategy> strategies) {
        this.strategies = strategies;
    }

    public String evaluarRiesgoHumano(Diagnostico diagnostico) {
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

        String result = null;
        for (RiskStrategy strategy : strategies) {
            if (strategy.canHandle(diagnostico)) {
                result = strategy.evaluate(diagnostico);
                if (result != null) {
                    break;
                }
            }
        }

        if (result != null) {
            alerta.append(result);
        } else {
            alerta.append("NIVEL: MEDIO\nConvivencia normal. Mantenga las medidas preventivas generales.\n\n");
        }

        alerta.append("ACCIONES RECOMENDADAS:\n").append(parasito.getMedidasPreventivas());
        return alerta.toString();
    }
}
