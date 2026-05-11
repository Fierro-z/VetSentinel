import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ConexionDB {

    // El archivo .db se crea solo en la carpeta del proyecto
    private static final String URL = "jdbc:sqlite:vetsentimel.db";

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite no encontrado. Agrega el .jar al proyecto.");
        }
        return DriverManager.getConnection(URL);
    }

    // Crea las tablas y datos base si no existen
    public static void inicializarBD() {
        try (Connection con = getConexion(); Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS Parasitos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "riesgo_principal TEXT," +
                    "medidas_preventivas TEXT," +
                    "alerta_embarazo INTEGER DEFAULT 0," +
                    "alerta_ninos INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Propietarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cedula TEXT UNIQUE," +
                    "nombre TEXT NOT NULL," +
                    "direccion TEXT," +
                    "tiene_ninos INTEGER DEFAULT 0," +
                    "hay_embarazadas INTEGER DEFAULT 0," +
                    "numero_embarazos_previos INTEGER DEFAULT 0," +
                    "zona_rural INTEGER DEFAULT 0)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Mascotas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "especie TEXT," +
                    "edad INTEGER," +
                    "id_propietario INTEGER," +
                    "FOREIGN KEY (id_propietario) REFERENCES Propietarios(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Diagnosticos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_mascota INTEGER," +
                    "id_parasito INTEGER," +
                    "fecha TEXT," +
                    "estado_contagio TEXT," +
                    "nivel_riesgo TEXT," +
                    "FOREIGN KEY (id_mascota) REFERENCES Mascotas(id)," +
                    "FOREIGN KEY (id_parasito) REFERENCES Parasitos(id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)");

            // Intento seguro de migrar bases de datos existentes
            try { stmt.execute("ALTER TABLE Diagnosticos ADD COLUMN nivel_riesgo TEXT"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN cedula TEXT"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_embarazo INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_ninos INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_zona_rural INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN zona_rural INTEGER DEFAULT 0"); } catch (SQLException ignore) {}
            try { stmt.execute("ALTER TABLE Propietarios ADD COLUMN numero_embarazos_previos INTEGER DEFAULT 0"); } catch (SQLException ignore) {}

            // Eliminar parásitos que no sean los 3 permitidos (Toxoplasmosis, Leishmaniasis, Toxocariasis)
            stmt.execute("DELETE FROM Parasitos WHERE nombre NOT LIKE '%Toxoplasma%' AND nombre NOT LIKE '%Leishmania%' AND nombre NOT LIKE '%Toxocara%'");

            // Datos de los 3 parásitos/enfermedades permitidos
            String[][] parasitosData = {
                {"Toxoplasmosis", "Infección causada por el parásito Toxoplasma gondii. Se transmite por heces de gatos, carne mal cocida o alimentos/agua contaminados. Es muy común y peligrosa especialmente para mujeres embarazadas porque puede afectar al bebé.", "Evitar que la mujer embarazada manipule la caja de arena del gato y asegurar que la carne consumida en el hogar esté bien cocida.", "1", "0", "0"},
                {"Leishmaniasis", "Enfermedad parasitaria transmitida por la picadura de un insecto. En Colombia es frecuente la forma cutánea, que produce lesiones en la piel y es común en zonas rurales y selváticas.", "Control de vectores y uso de toldillos, especialmente si la mascota duerme dentro o cerca de la casa. Fumigación peridomiciliar.", "0", "1", "1"},
                {"Toxocariasis", "Infección causada por parásitos de perros y gatos. Las personas se contagian al ingerir huevos presentes en suelo contaminado, especialmente en parques. Es común en niños.", "Desparasitar mascota cada 3 meses. Evitar contacto de niños con suelo contaminado.", "0", "1", "0"}
            };

            for (String[] pData : parasitosData) {
                String nombre = pData[0];
                String riesgo = pData[1];
                String medidas = pData[2];
                int alertaEmbarazo = Integer.parseInt(pData[3]);
                int alertaNinos = Integer.parseInt(pData[4]);
                int alertaZonaRural = Integer.parseInt(pData[5]);

                // Buscar si ya existe
                PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Parasitos WHERE nombre LIKE ?");
                psCheck.setString(1, "%" + nombre.substring(0, 5) + "%");
                ResultSet rsCheck = psCheck.executeQuery();

                if (rsCheck.next()) {
                    int id = rsCheck.getInt(1);
                    PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Parasitos SET nombre = ?, riesgo_principal = ?, medidas_preventivas = ?, alerta_embarazo = ?, alerta_ninos = ?, alerta_zona_rural = ? WHERE id = ?");
                    psUpd.setString(1, nombre);
                    psUpd.setString(2, riesgo);
                    psUpd.setString(3, medidas);
                    psUpd.setInt(4, alertaEmbarazo);
                    psUpd.setInt(5, alertaNinos);
                    psUpd.setInt(6, alertaZonaRural);
                    psUpd.setInt(7, id);
                    psUpd.executeUpdate();
                } else {
                    PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos, alerta_zona_rural) VALUES (?, ?, ?, ?, ?, ?)");
                    psIns.setString(1, nombre);
                    psIns.setString(2, riesgo);
                    psIns.setString(3, medidas);
                    psIns.setInt(4, alertaEmbarazo);
                    psIns.setInt(5, alertaNinos);
                    psIns.setInt(6, alertaZonaRural);
                    psIns.executeUpdate();
                }
            }

            var rsUsr = stmt.executeQuery("SELECT COUNT(*) FROM Usuarios");
            if (rsUsr.next() && rsUsr.getInt(1) == 0) {
                stmt.execute("INSERT INTO Usuarios (username, password) VALUES ('admin', 'admin123')");
            }

            System.out.println("Base de datos lista: vetsentimel.db");

        } catch (SQLException e) {
            System.out.println("Error al inicializar BD: " + e.getMessage());
        }
    }

}