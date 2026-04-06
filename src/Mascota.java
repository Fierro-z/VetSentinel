public class Mascota {
    private int id;
    private String nombre;
    private String especie;
    private int edad;
    private Propietario propietario;

    public Mascota(int id, String nombre, String especie, int edad, Propietario propietario) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.edad = edad;
        this.propietario = propietario;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecie() { return especie; }
    public int getEdad() { return edad; }
    public Propietario getPropietario() { return propietario; }
}