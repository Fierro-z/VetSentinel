public class Propietario {
    private int id;
    private String cedula;
    private String nombre;
    private String direccion;
    private String departamento;
    private boolean tieneNinos;
    private boolean hayEmbarazadas;
    private int numeroDeEmbarazosPrevios;
    private boolean zonaRural;
    private int estrato;
    private String regimen;
    private int altitud;

    public Propietario(int id, String cedula, String nombre, String direccion, String departamento, boolean tieneNinos,
            boolean hayEmbarazadas, int numeroDeEmbarazosPrevios, boolean zonaRural, int estrato, String regimen, int altitud) {
        this.id = id;
        this.cedula = cedula;
        this.nombre = nombre;
        this.direccion = direccion;
        this.departamento = departamento;
        this.tieneNinos = tieneNinos;
        this.hayEmbarazadas = hayEmbarazadas;
        this.numeroDeEmbarazosPrevios = numeroDeEmbarazosPrevios;
        this.zonaRural = zonaRural;
        this.estrato = estrato;
        this.regimen = regimen;
        this.altitud = altitud;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public boolean isTieneNinos() {
        return tieneNinos;
    }

    public boolean isHayEmbarazadas() {
        return hayEmbarazadas;
    }

    public int getNumeroDeEmbarazosPrevios() {
        return numeroDeEmbarazosPrevios;
    }

    public boolean isZonaRural() {
        return zonaRural;
    }

    public int getEstrato() {
        return estrato;
    }

    public void setEstrato(int estrato) {
        this.estrato = estrato;
    }

    public String getRegimen() {
        return regimen;
    }

    public void setRegimen(String regimen) {
        this.regimen = regimen;
    }

    public int getAltitud() {
        return altitud;
    }

    public void setAltitud(int altitud) {
        this.altitud = altitud;
    }
}