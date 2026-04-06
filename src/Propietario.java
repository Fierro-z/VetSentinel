public class Propietario {
    private int id;
    private String nombre;
    private String direccion;
    private boolean tieneNinos;
    private boolean hayEmbarazadas;

    public Propietario(int id, String nombre, String direccion, boolean tieneNinos, boolean hayEmbarazadas) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.tieneNinos = tieneNinos;
        this.hayEmbarazadas = hayEmbarazadas;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public boolean isTieneNinos() { return tieneNinos; }
    public boolean isHayEmbarazadas() { return hayEmbarazadas; }
}