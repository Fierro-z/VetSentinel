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
            if (dueno.isTieneNinos() && dueno.isZonaRural()) {
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
            
            if (esToxoplasma && dueno.getNumeroDeEmbarazosPrevios() > 1) {
                alerta += "NIVEL: CRITICO (ALTA EXPOSICIÓN EPIDEMIOLÓGICA)\n";
                alerta += "Riesgo crítico: Gestante multípara. Por datos epidemiológicos, las mujeres con embarazos previos tienen mayor probabilidad de exposición acumulada al parásito en su entorno.\n";
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
