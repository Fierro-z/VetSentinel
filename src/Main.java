public class Main {
    public static void main(String[] args) {
        // Crea las tablas y datos base automáticamente
        ConexionDB.inicializarBD();

        // Muestra la ventana principal
        VentanaVeterinaria ventana = new VentanaVeterinaria();
        ventana.setVisible(true);
    }
}