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



    public int getId() { return id; }
    public Mascota getMascota() { return mascota; }
    public Parasito getParasito() { return parasito; }
    public String getEstadoContagio() { return estadoContagio; }
    public String getFecha() { return fecha; }
}