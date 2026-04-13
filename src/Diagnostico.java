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

        if (parasito.isAlertaEmbarazo() && dueno.isHayEmbarazadas()) {
            alerta += "NIVEL: CRITICO\n";
            alerta += "Riesgo crítico por factor embarazo: " + parasito.getRiesgoPrincipal() + "\n\n";
        } else if (parasito.isAlertaNinos() && dueno.isTieneNinos()) {
            alerta += "NIVEL: ALTO\n";
            alerta += "Riesgo elevado por presencia de menores: " + parasito.getRiesgoPrincipal() + "\n\n";
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