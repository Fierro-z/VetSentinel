public class Diagnostico {
    private int id;
    private Mascota mascota;
    private Parasito parasito;
    private String fecha;
    private String estadoContagio;

    public Diagnostico(int id, Mascota mascota, Parasito parasito, String fecha, String estadoContagio) {
        this.id = id;
        this.mascota = mascota;
        this.parasito = parasito;
        this.fecha = fecha;
        this.estadoContagio = estadoContagio;
    }

    public String evaluarRiesgoHumano() {
        Propietario dueno = mascota.getPropietario();
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
            if (esToxoplasma && dueno.getNumeroDeEmbarazosPrevios() >= 2) {
                alerta += "NIVEL: CRITICO (ALTA EXPOSICIÓN EPIDEMIOLÓGICA)\n";
                alerta += "Riesgo crítico: Gestante multípara (" + dueno.getNumeroDeEmbarazosPrevios() + " embarazos previos). El estudio del Hospital Hernando Moncaleano (Neiva, Huila) indica una alta prevalencia de exposición a Toxoplasma (56.7%) en multíparas. Requiere tamizaje serológico (IgG/IgM) urgente.\n\n";
            } else if (esToxoplasma) {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Riesgo crítico por embarazo. Prevalencia de exposición a Toxoplasma en primíparas: 50.3%. " + parasito.getRiesgoPrincipal() + "\n\n";
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

    public int getId() { return id; }
    public Mascota getMascota() { return mascota; }
    public Parasito getParasito() { return parasito; }
    public String getEstadoContagio() { return estadoContagio; }
    public String getFecha() { return fecha; }
}