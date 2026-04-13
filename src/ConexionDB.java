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
                stmt.execute("ALTER TABLE Propietarios ADD COLUMN cedula TEXT UNIQUE");
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

    public static int upsertPropietario(Propietario p) throws SQLException {
        try (Connection con = getConexion()) {
            PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Propietarios WHERE cedula = ?");
            psCheck.setString(1, p.getCedula());
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Propietarios SET nombre = ?, direccion = ?, tiene_ninos = ?, hay_embarazadas = ? WHERE id = ?");
                psUpd.setString(1, p.getNombre());
                psUpd.setString(2, p.getDireccion());
                psUpd.setInt(3, p.isTieneNinos() ? 1 : 0);
                psUpd.setInt(4, p.isHayEmbarazadas() ? 1 : 0);
                psUpd.setInt(5, id);
                psUpd.executeUpdate();
                return id;
            } else {
                PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Propietarios (cedula, nombre, direccion, tiene_ninos, hay_embarazadas) VALUES (?, ?, ?, ?, ?)");
                psIns.setString(1, p.getCedula());
                psIns.setString(2, p.getNombre());
                psIns.setString(3, p.getDireccion());
                psIns.setInt(4, p.isTieneNinos() ? 1 : 0);
                psIns.setInt(5, p.isHayEmbarazadas() ? 1 : 0);
                psIns.executeUpdate();
                ResultSet keys = con.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Fallo al upsertar propietario");
    }

    public static int upsertMascota(Mascota m) throws SQLException {
        try (Connection con = getConexion()) {
            PreparedStatement psCheck = con.prepareStatement(
                    "SELECT id FROM Mascotas WHERE nombre = ? AND id_propietario = ?");
            psCheck.setString(1, m.getNombre());
            psCheck.setInt(2, m.getPropietario().getId());
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Mascotas SET especie = ?, edad = ? WHERE id = ?");
                psUpd.setString(1, m.getEspecie());
                psUpd.setInt(2, m.getEdad());
                psUpd.setInt(3, id);
                psUpd.executeUpdate();
                return id;
            } else {
                PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Mascotas (nombre, especie, edad, id_propietario) VALUES (?, ?, ?, ?)");
                psIns.setString(1, m.getNombre());
                psIns.setString(2, m.getEspecie());
                psIns.setInt(3, m.getEdad());
                psIns.setInt(4, m.getPropietario().getId());
                psIns.executeUpdate();
                ResultSet keys = con.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Fallo al upsertar mascota");
    }

    public static void insertarDiagnostico(int idMasc, int idPar, String nivelRiesgo) throws SQLException {
        try (Connection con = getConexion()) {
            PreparedStatement psDiag = con.prepareStatement(
                    "INSERT INTO Diagnosticos (id_mascota, id_parasito, fecha, estado_contagio, nivel_riesgo) VALUES (?,?,date('now'),'Activo',?)");
            psDiag.setInt(1, idMasc);
            psDiag.setInt(2, idPar);
            psDiag.setString(3, nivelRiesgo);
            psDiag.executeUpdate();
        }
    }
}