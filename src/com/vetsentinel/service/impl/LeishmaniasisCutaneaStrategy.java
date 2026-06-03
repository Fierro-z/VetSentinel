package com.vetsentinel.service.impl;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.model.RiskLevel;
import com.vetsentinel.model.RiskResult;
import com.vetsentinel.service.RiskStrategy;

public class LeishmaniasisCutaneaStrategy implements RiskStrategy {

    @Override
    public boolean canHandle(Diagnostico diagnostico) {
        String name = diagnostico.getParasito().getNombre().toLowerCase();
        return name.contains("leishmania") && (name.contains("cutánea") || name.contains("cutanea"));
    }

    @Override
    public RiskResult evaluate(Diagnostico diagnostico) {
        Propietario dueno = diagnostico.getMascota().getPropietario();
        String dept = dueno.getDepartamento();
        if (dueno.isTieneNinos() && 
            ("Risaralda".equalsIgnoreCase(dept) || "Atlántico".equalsIgnoreCase(dept) || "Atlantico".equalsIgnoreCase(dept) || "Caldas".equalsIgnoreCase(dept))) {
            return new RiskResult(RiskLevel.CRITICO,
                   "Riesgo Crítico: Menores de edad expuestos en departamento de alta prevalencia domiciliaria (" + dept + "). La presencia de niños menores de 10 años infectados es indicador clave de transmisión intra o peridomiciliaria.\n\n");
        }
        return null;
    }
}
