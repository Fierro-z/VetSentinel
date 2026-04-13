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

        if (p.equals("Toxoplasma gondii") && mascota.getEspecie().equalsIgnoreCase("Gato")) {
            if (dueno.isHayEmbarazadas()) {
                alerta += "NIVEL: CRITICO\n";
                alerta += "Hay embarazada en el hogar con gato positivo a Toxoplasma.\n";
                alerta += "Riesgo de TOXOPLASMOSIS CONGENITA (daño fetal).\n\n";
            } else {
                alerta += "NIVEL: MEDIO\n";
                alerta += "Gato positivo a Toxoplasma. Mantener higiene.\n\n";
            }
        } else if (p.equals("Toxocara canis/cati")) {
            if (dueno.isTieneNinos()) {
                alerta += "NIVEL: ALTO\n";
                alerta += "Hay niños en el hogar. Riesgo de LARVA MIGRANS VISCERAL.\n";
                alerta += "Vigilar GEOFAGIA (niños que comen tierra).\n\n";
            } else {
                alerta += "NIVEL: MODERADO\n";
                alerta += "Desparasitar la mascota de inmediato.\n\n";
            }
        } else if (p.equals("Leishmania spp")) {
            alerta += "NIVEL: ALTO\n";
            alerta += "El perro es RESERVORIO de Leishmaniasis cutánea.\n";
            alerta += "El vector (Lutzomyia) puede picar a humanos del hogar.\n";
            if (dueno.isTieneNinos()) {
                alerta += "ATENCION: Niños en el hogar, usar repelente y toldillos.\n";
            }
            alerta += "\n";
        } else {
            alerta += "NIVEL: BAJO\n";
            alerta += "No se detectaron factores de riesgo críticos.\n\n";
        }

        alerta += "ACCIONES: " + parasito.getMedidasPreventivas();
        return alerta;
    }

    public int getId() { return id; }
    public Mascota getMascota() { return mascota; }
    public Parasito getParasito() { return parasito; }
    public String getEstadoContagio() { return estadoContagio; }
    public String getFecha() { return fecha; }
}