public class Propietario {
    private int id;
    private String cedula;
    private String nombre;
    private String direccion;
    private boolean tieneNinos;
    private boolean hayEmbarazadas;

    public Propietario(int id, String cedula, String nombre, String direccion, boolean tieneNinos, boolean hayEmbarazadas) {
        this.id = id;
        this.cedula = cedula;
        this.nombre = nombre;
        this.direccion = direccion;
        this.tieneNinos = tieneNinos;
        this.hayEmbarazadas = hayEmbarazadas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public boolean isTieneNinos() { return tieneNinos; }
    public boolean isHayEmbarazadas() { return hayEmbarazadas; }
}