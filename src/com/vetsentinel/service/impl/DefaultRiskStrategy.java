package com.vetsentinel.service.impl;

import com.vetsentinel.model.Diagnostico;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Parasito;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.service.RiskStrategy;

public class DefaultRiskStrategy implements RiskStrategy {

    @Override
    public boolean canHandle(Diagnostico diagnostico) {
        return true; // Catch-all fallback strategy
    }

    @Override
    public String evaluate(Diagnostico diagnostico) {
        Mascota mascota = diagnostico.getMascota();
        Propietario dueno = mascota.getPropietario();
        Parasito parasito = diagnostico.getParasito();
        String p = parasito.getNombre().toLowerCase();

        boolean esLeishmaniasis = p.contains("leishmania");

        if (esLeishmaniasis) {
            if (dueno.isTieneNinos() && dueno.isZonaRural()) {
                return "NIVEL: CRITICO\n" +
                       "Riesgo inminente: Presencia de niños en zona rural (factor de riesgo del 82.7%). Transmisión peridomiciliaria (Lutzomyia sp.) muy probable.\n\n";
            } else if (dueno.isTieneNinos()) {
                return "NIVEL: ALTO\n" +
                       "Riesgo elevado: Niños en el hogar (9.4% de casos en menores). " + parasito.getRiesgoPrincipal() + "\n\n";
            } else if (dueno.isZonaRural()) {
                return "NIVEL: ALTO\n" +
                       "Riesgo elevado: Residencia en zona rural (82.7% de los casos). " + parasito.getRiesgoPrincipal() + "\n\n";
            } else {
                return "NIVEL: MEDIO\n" +
                       "Riesgo general de Leishmaniasis: " + parasito.getRiesgoPrincipal() + "\n\n";
            }
        } else if (parasito.isAlertaEmbarazo() && dueno.isHayEmbarazadas()) {
            boolean esGato = mascota.esGato();
            String res = "NIVEL: CRITICO\n" +
                   "Riesgo crítico por embarazo. Requiere control preventivo de exposición a Toxoplasma. " + parasito.getRiesgoPrincipal() + "\n";
            if (esGato) {
                res += "¡ALERTA FELINA CRÍTICA! Transmisión directa por ooquistes en heces de gatos.\n\n";
            } else {
                res += "\n";
            }
            return res;
        } else if (parasito.isAlertaNinos() && dueno.isTieneNinos()) {
            return "NIVEL: ALTO\n" +
                   "Riesgo elevado por presencia de menores: " + parasito.getRiesgoPrincipal() + "\n\n";
        } else if (parasito.isAlertaZonaRural() && dueno.isZonaRural()) {
            return "NIVEL: ALTO\n" +
                   "Riesgo elevado por residencia en zona rural: " + parasito.getRiesgoPrincipal() + "\n\n";
        } else {
            return "NIVEL: MEDIO\n" +
                   "Riesgo general: " + parasito.getRiesgoPrincipal() + "\n\n";
        }
    }
}
