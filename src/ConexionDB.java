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
                    "nivel_riesgo TEXT," +
                    "FOREIGN KEY (id_mascota) REFERENCES Mascotas(id)," +
                    "FOREIGN KEY (id_parasito) REFERENCES Parasitos(id))");

            // Intento seguro de migrar bases de datos existentes
            try {
                stmt.execute("ALTER TABLE Diagnosticos ADD COLUMN nivel_riesgo TEXT");
            } catch (SQLException ignore) {}

            try {
                stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_embarazo INTEGER DEFAULT 0");
                stmt.execute("ALTER TABLE Parasitos ADD COLUMN alerta_ninos INTEGER DEFAULT 0");
                
                // Actualizar las banderas de los seeds originales si la migración de esquema acaba de ocurrir
                stmt.execute("UPDATE Parasitos SET alerta_embarazo = 1 WHERE nombre = 'Toxoplasma gondii'");
                stmt.execute("UPDATE Parasitos SET alerta_ninos = 1 WHERE nombre = 'Leishmania spp'");
                stmt.execute("UPDATE Parasitos SET alerta_ninos = 1 WHERE nombre = 'Toxocara canis/cati'");
            } catch (SQLException ignore) {}

            // Insertar parásitos base solo si la tabla está vacía
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Parasitos");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos) VALUES " +
                        "('Toxoplasma gondii', 'Transmision congenita. Seroprevalencia 40-60% en Colombia (INS).', 'No limpiar arenero sin guantes. Cocinar carne a mas de 70 grados. Lavar vegetales.', 1, 0)");
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos) VALUES " +
                        "('Leishmania spp', 'Leishmaniasis cutanea vectorial. Endemica en Antioquia (INS BES SE26 2025).', 'Control del vector Lutzomyia. Uso de toldillos y repelente. Fumigacion peridomiciliar.', 0, 1)");
                stmt.execute("INSERT INTO Parasitos (nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos) VALUES " +
                        "('Toxocara canis/cati', 'Larva migrans visceral/cutanea en ninos. Prevalencia perros 7-20% (INS).', 'Desparasitar mascota cada 3 meses. Evitar contacto de ninos con suelo contaminado.', 0, 1)");
            }

            System.out.println("Base de datos lista: vetsentimel.db");

        } catch (SQLException e) {
            System.out.println("Error al inicializar BD: " + e.getMessage());
        }
    }

    public static List<Parasito> obtenerTodosLosParasitos() {
        List<Parasito> lista = new ArrayList<>();
        try (Connection con = getConexion(); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos FROM Parasitos");
            while (rs.next()) {
                lista.add(new Parasito(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("riesgo_principal"),
                        rs.getString("medidas_preventivas"),
                        rs.getInt("alerta_embarazo") == 1,
                        rs.getInt("alerta_ninos") == 1
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener parásitos: " + e.getMessage());
        }
        return lista;
    }
}