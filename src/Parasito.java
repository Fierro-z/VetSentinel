public class Parasito {
    private int id;
    private String nombre;
    private String riesgoPrincipal;
    private String medidasPreventivas;

    public Parasito(int id, String nombre, String riesgoPrincipal, String medidasPreventivas) {
        this.id = id;
        this.nombre = nombre;
        this.riesgoPrincipal = riesgoPrincipal;
        this.medidasPreventivas = medidasPreventivas;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRiesgoPrincipal() { return riesgoPrincipal; }
    public String getMedidasPreventivas() { return medidasPreventivas; }
}