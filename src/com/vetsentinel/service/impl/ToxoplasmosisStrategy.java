package com.vetsentinel.service.impl;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.service.RiskStrategy;

public class ToxoplasmosisStrategy implements RiskStrategy {

    @Override
    public boolean canHandle(Diagnostico diagnostico) {
        String name = diagnostico.getParasito().getNombre().toLowerCase();
        return name.contains("toxoplasma");
    }

    @Override
    public String evaluate(Diagnostico diagnostico) {
        Mascota mascota = diagnostico.getMascota();
        Propietario dueno = mascota.getPropietario();
        
        if (diagnostico.getParasito().isAlertaEmbarazo() && dueno.isHayEmbarazadas()) {
            if (dueno.getNumeroDeEmbarazosPrevios() >= 2) {
                String res = "NIVEL: CRITICO (ALTA EXPOSICIÓN EPIDEMIOLÓGICA)\n" +
                       "Riesgo crítico: Gestante multípara (>= 2 embarazos previos). El estudio de seroprevalencia en Huila reporta un 56.7% de prevalencia en gestantes multíparas, reflejando mayor exposición acumulada al parásito.\n";
                if (mascota.esGato()) {
                    res += "¡ALERTA FELINA CRÍTICA! Transmisión directa por ooquistes en heces de gatos.\n\n";
                } else {
                    res += "\n";
                }
                return res;
            }
        }
        return null;
    }
}
