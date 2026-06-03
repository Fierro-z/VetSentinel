package com.vetsentinel.service.impl;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Parasito;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.model.RiskLevel;
import com.vetsentinel.model.RiskResult;
import com.vetsentinel.service.RiskStrategy;

public class DefaultRiskStrategy implements RiskStrategy {

    @Override
    public boolean canHandle(Diagnostico diagnostico) {
        return true; // Catch-all fallback strategy
    }

    @Override
    public RiskResult evaluate(Diagnostico diagnostico) {
        Mascota mascota = diagnostico.getMascota();
        Propietario dueno = mascota.getPropietario();
        Parasito parasito = diagnostico.getParasito();
        String p = parasito.getNombre().toLowerCase();

        boolean esLeishmaniasis = p.contains("leishmania");

        if (esLeishmaniasis) {
            if (dueno.isTieneNinos() && dueno.isZonaRural()) {
                return new RiskResult(RiskLevel.CRITICO,
                       "Riesgo inminente: Presencia de niños en zona rural (factor de riesgo del 82.7%). Transmisión peridomiciliaria (Lutzomyia sp.) muy probable.\n\n");
            } else if (dueno.isTieneNinos()) {
                return new RiskResult(RiskLevel.ALTO,
                       "Riesgo elevado: Niños en el hogar (9.4% de casos en menores). " + parasito.getRiesgoPrincipal() + "\n\n");
            } else if (dueno.isZonaRural()) {
                return new RiskResult(RiskLevel.ALTO,
                       "Riesgo elevado: Residencia en zona rural (82.7% de los casos). " + parasito.getRiesgoPrincipal() + "\n\n");
            } else {
                return new RiskResult(RiskLevel.MEDIO,
                       "Riesgo general de Leishmaniasis: " + parasito.getRiesgoPrincipal() + "\n\n");
            }
        } else if (parasito.isAlertaEmbarazo() && dueno.isHayEmbarazadas()) {
            boolean esGato = mascota.esGato();
            String res = "Riesgo crítico por embarazo. Requiere control preventivo de exposición a Toxoplasma. " + parasito.getRiesgoPrincipal() + "\n";
            if (esGato) {
                res += "¡ALERTA FELINA CRÍTICA! Transmisión directa por ooquistes en heces de gatos.\n\n";
            } else {
                res += "\n";
            }
            return new RiskResult(RiskLevel.CRITICO, res);
        } else if (parasito.isAlertaNinos() && dueno.isTieneNinos()) {
            return new RiskResult(RiskLevel.ALTO,
                   "Riesgo elevado por presencia de menores: " + parasito.getRiesgoPrincipal() + "\n\n");
        } else if (parasito.isAlertaZonaRural() && dueno.isZonaRural()) {
            return new RiskResult(RiskLevel.ALTO,
                   "Riesgo elevado por residencia en zona rural: " + parasito.getRiesgoPrincipal() + "\n\n");
        } else {
            return new RiskResult(RiskLevel.MEDIO,
                   "Riesgo general: " + parasito.getRiesgoPrincipal() + "\n\n");
        }
    }
}
