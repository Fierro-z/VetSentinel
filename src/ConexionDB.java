import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
                    "medidas_preventivas TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Propietarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL," +
                    "direccion TEXT," +
                    "tiene_ninos INTEGER DEFAULT 0," +
                    "hay_embarazadas INTEGER DEFAULT 0)");

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
                    "FOREIGN KEY (id_mascota) REFERENCES Mascotas(id)," +
                    "FOREIGN KEY (id_parasito) REFERENCES Parasitos(id))");

            // Insertar parásitos base solo si la tabla está vacía
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Parasitos");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas) VALUES " +
                        "('Toxoplasma gondii', 'Transmision congenita. Seroprevalencia 40-60% en Colombia (INS).', 'No limpiar arenero sin guantes. Cocinar carne a mas de 70 grados. Lavar vegetales.')");
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas) VALUES " +
                        "('Leishmania spp', 'Leishmaniasis cutanea vectorial. Endemica en Antioquia (INS BES SE26 2025).', 'Control del vector Lutzomyia. Uso de toldillos y repelente. Fumigacion peridomiciliar.')");
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas) VALUES " +
                        "('Toxocara canis/cati', 'Larva migrans visceral/cutanea en ninos. Prevalencia perros 7-20% (INS).', 'Desparasitar mascota cada 3 meses. Evitar contacto de ninos con suelo contaminado.')");
            }

            System.out.println("Base de datos lista: vetsentimel.db");

        } catch (SQLException e) {
            System.out.println("Error al inicializar BD: " + e.getMessage());
        }
    }
}