public class Main {
    public static void main(String[] args) {
        // Crea las tablas y datos base automáticamente
        ConexionDB.inicializarBD();

        // Muestra la ventana de login
        VentanaLogin login = new VentanaLogin();
        login.setVisible(true);
    }
}