public class RiesgoService {

    public static String evaluarRiesgoHumano(Diagnostico diagnostico) {
        Mascota mascota = diagnostico.getMascota();
        Propietario dueno = mascota.getPropietario();
        Parasito parasito = diagnostico.getParasito();
        String p = parasito.getNombre();

        String alerta = "=== ALERTA DE CONVIVENCIA - VetSentinel ===\n\n";
        alerta += "Mascota: " + mascota.getNombre() + " (" + mascota.getEspecie() + ", " + mascota.getEdad() + " años)\n";
        alerta += "Propietario: " + dueno.getNombre() + "\n";
        alerta += "Dirección: " + dueno.getDireccion() + "\n";
        alerta += "Parásito detectado: " + p + "\n\n";

        boolean esLeishmaniasis = p.toLowerCase().contains("leishmania");

        if (esLeishmaniasis) {
            boolean esVisceral = p.toLowerCase().contains("visceral");
            boolean esCutanea = p.toLowerCase().contains("cutánea") || p.toLowerCase().contains("cutanea");

            if (esVisceral && (dueno.getEstrato() == 1 || dueno.getRegimen().equalsIgnoreCase("Subsidiado"))) {
                alerta += "NIVEL: EMERGENCIA CRÍTICA\n";
                alerta += "Alerta de Emergencia: Paciente vulnerable (Estrato 1 / Régimen Subsidiado) expuesto a Leishmaniasis Visceral. La letalidad de esta cepa supera el 95% sin tratamiento. Antecedentes del INS reportan casos graves en niños de 5 años y lactantes de 6 meses en Sucre.\n\n";
            } else if (esCutanea && dueno.isTieneNinos() && 
                       (dueno.getDepartamento().equalsIgnoreCase("Risaralda") || 
                        dueno.getDepartamento().equalsIgnoreCase("Atlántico") || 
                        dueno.getDepartamento().equalsIgnoreCase("Atlantico") || 
                        dueno.getDepartamento().equalsIgnoreCase("Caldas"))) {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Riesgo Crítico: Menores de edad expuestos en departamento de alta prevalencia domiciliaria (" + dueno.getDepartamento() + "). La presencia de niños menores de 10 años infectados es indicador clave de transmisión intra o peridomiciliaria.\n\n";
            } else if (dueno.isTieneNinos() && dueno.isZonaRural()) {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Riesgo inminente: Presencia de niños en zona rural (factor de riesgo del 82.7%). Transmisión peridomiciliaria (Lutzomyia sp.) muy probable.\n\n";
            } else if (dueno.isTieneNinos()) {
                alerta += "NIVEL: ALTO\n";
                alerta += "Riesgo elevado: Niños en el hogar (9.4% de casos en menores). " + parasito.getRiesgoPrincipal() + "\n\n";
            } else if (dueno.isZonaRural()) {
                alerta += "NIVEL: ALTO\n";
                alerta += "Riesgo elevado: Residencia en zona rural (82.7% de los casos). " + parasito.getRiesgoPrincipal() + "\n\n";
            } else {
                alerta += "NIVEL: MEDIO\n";
                alerta += "Riesgo general de Leishmaniasis: " + parasito.getRiesgoPrincipal() + "\n\n";
            }
        } else if (parasito.isAlertaEmbarazo() && dueno.isHayEmbarazadas()) {
            boolean esToxoplasma = p.toLowerCase().contains("toxoplasma");
            boolean esGato = mascota.getEspecie().equalsIgnoreCase("Gato");
            
            if (esToxoplasma && dueno.getNumeroDeEmbarazosPrevios() >= 2) {
                alerta += "NIVEL: CRITICO (ALTA EXPOSICIÓN EPIDEMIOLÓGICA)\n";
                alerta += "Riesgo crítico: Gestante multípara (>= 2 embarazos previos). El estudio de seroprevalencia en Huila reporta un 56.7% de prevalencia en gestantes multíparas, reflejando mayor exposición acumulada al parásito.\n";
                if (esGato) alerta += "¡ALERTA FELINA CRÍTICA! Transmisión directa por ooquistes en heces de gatos.\n\n";
                else alerta += "\n";
            } else if (esToxoplasma) {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Riesgo crítico por embarazo. Requiere control preventivo de exposición a Toxoplasma. " + parasito.getRiesgoPrincipal() + "\n";
                if (esGato) alerta += "¡ALERTA FELINA CRÍTICA! Transmisión directa por ooquistes en heces de gatos.\n\n";
                else alerta += "\n";
            } else {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Riesgo crítico por factor embarazo: " + parasito.getRiesgoPrincipal() + "\n\n";
            }
        } else if (parasito.isAlertaNinos() && dueno.isTieneNinos()) {
            alerta += "NIVEL: ALTO\n";
            alerta += "Riesgo elevado por presencia de menores: " + parasito.getRiesgoPrincipal() + "\n\n";
        } else if (parasito.isAlertaZonaRural() && dueno.isZonaRural()) {
            alerta += "NIVEL: ALTO\n";
            alerta += "Riesgo elevado por residencia en zona rural: " + parasito.getRiesgoPrincipal() + "\n\n";
        } else {
            alerta += "NIVEL: MEDIO\n";
            alerta += "Riesgo general: " + parasito.getRiesgoPrincipal() + "\n\n";
        }

        alerta += "ACCIONES RECOMENDADAS:\n" + parasito.getMedidasPreventivas();
        return alerta;
    }
}
