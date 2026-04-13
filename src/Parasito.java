public class Parasito {
    private int id;
    private String nombre;
    private String riesgoPrincipal;
    private String medidasPreventivas;
    private boolean alertaEmbarazo;
    private boolean alertaNinos;

    public Parasito(int id, String nombre, String riesgoPrincipal, String medidasPreventivas, boolean alertaEmbarazo, boolean alertaNinos) {
        this.id = id;
        this.nombre = nombre;
        this.riesgoPrincipal = riesgoPrincipal;
        this.medidasPreventivas = medidasPreventivas;
        this.alertaEmbarazo = alertaEmbarazo;
        this.alertaNinos = alertaNinos;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRiesgoPrincipal() { return riesgoPrincipal; }
    public String getMedidasPreventivas() { return medidasPreventivas; }
    public boolean isAlertaEmbarazo() { return alertaEmbarazo; }
    public boolean isAlertaNinos() { return alertaNinos; }

    @Override
    public String toString() {
        return nombre;
    }
}