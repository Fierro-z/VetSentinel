package com.vetsentinel.service.impl;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.service.RiskStrategy;

public class LeishmaniasisVisceralStrategy implements RiskStrategy {

    @Override
    public boolean canHandle(Diagnostico diagnostico) {
        String name = diagnostico.getParasito().getNombre().toLowerCase();
        return name.contains("leishmania") && name.contains("visceral");
    }

    @Override
    public String evaluate(Diagnostico diagnostico) {
        Propietario dueno = diagnostico.getMascota().getPropietario();
        if (dueno.getEstrato() == 1 || "Subsidiado".equalsIgnoreCase(dueno.getRegimen())) {
            return "NIVEL: EMERGENCIA CRÍTICA\n" +
                   "Alerta de Emergencia: Paciente vulnerable (Estrato 1 / Régimen Subsidiado) expuesto a Leishmaniasis Visceral. La letalidad de esta cepa supera el 95% sin tratamiento. Antecedentes del INS reportan casos graves en niños de 5 años y lactantes de 6 meses en Sucre.\n\n";
        }
        return null; // Delegate to subsequent strategies if conditions not met
    }
}
