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

    public static List<Parasito> obtenerTodosLosParasitos() {
        List<Parasito> lista = new ArrayList<>();
        try (Connection con = getConexion(); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id, nombre, riesgo_principal, medidas_preventivas, alerta_embarazo, alerta_ninos, alerta_zona_rural FROM Parasitos");
            while (rs.next()) {
                lista.add(new Parasito(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("riesgo_principal"),
                        rs.getString("medidas_preventivas"),
                        rs.getInt("alerta_embarazo") == 1,
                        rs.getInt("alerta_ninos") == 1,
                        rs.getInt("alerta_zona_rural") == 1
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener parásitos: " + e.getMessage());
        }
        return lista;
    }

    public static String obtenerEstadisticasEpidemiologicas() {
        StringBuilder stats = new StringBuilder();
        try (Connection con = getConexion(); Statement stmt = con.createStatement()) {
            ResultSet rsEval = stmt.executeQuery("SELECT COUNT(*) FROM Mascotas");
            int totalMascotas = rsEval.next() ? rsEval.getInt(1) : 0;
            
            ResultSet rsCrit = stmt.executeQuery("SELECT COUNT(*) FROM Diagnosticos WHERE nivel_riesgo = 'CRITICO'");
            int totalCriticos = rsCrit.next() ? rsCrit.getInt(1) : 0;
            
            ResultSet rsPar = stmt.executeQuery("SELECT p.nombre, COUNT(d.id_parasito) as cant FROM Diagnosticos d JOIN Parasitos p ON d.id_parasito = p.id GROUP BY p.nombre ORDER BY cant DESC LIMIT 1");
            String parasitoComun = rsPar.next() ? rsPar.getString(1) : "N/A";
            
            stats.append("Total de mascotas evaluadas: ").append(totalMascotas).append("\n\n");
            stats.append("Total de diagnósticos críticos: ").append(totalCriticos).append("\n\n");
            stats.append("Parásito predominante en clínica: ").append(parasitoComun);
        } catch (SQLException e) {
            return "Error calculando estadísticas: " + e.getMessage();
        }
        return stats.toString();
    }

    public static int upsertPropietario(Propietario p) throws SQLException {
        try (Connection con = getConexion()) {
            PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Propietarios WHERE cedula = ?");
            psCheck.setString(1, p.getCedula());
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Propietarios SET nombre = ?, direccion = ?, tiene_ninos = ?, hay_embarazadas = ?, numero_embarazos_previos = ?, zona_rural = ? WHERE id = ?");
                psUpd.setString(1, p.getNombre());
                psUpd.setString(2, p.getDireccion());
                psUpd.setInt(3, p.isTieneNinos() ? 1 : 0);
                psUpd.setInt(4, p.isHayEmbarazadas() ? 1 : 0);
                psUpd.setInt(5, p.getNumeroDeEmbarazosPrevios());
                psUpd.setInt(6, p.isZonaRural() ? 1 : 0);
                psUpd.setInt(7, id);
                psUpd.executeUpdate();
                return id;
            } else {
                PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Propietarios (cedula, nombre, direccion, tiene_ninos, hay_embarazadas, numero_embarazos_previos, zona_rural) VALUES (?, ?, ?, ?, ?, ?, ?)");
                psIns.setString(1, p.getCedula());
                psIns.setString(2, p.getNombre());
                psIns.setString(3, p.getDireccion());
                psIns.setInt(4, p.isTieneNinos() ? 1 : 0);
                psIns.setInt(5, p.isHayEmbarazadas() ? 1 : 0);
                psIns.setInt(6, p.getNumeroDeEmbarazosPrevios());
                psIns.setInt(7, p.isZonaRural() ? 1 : 0);
                psIns.executeUpdate();
                ResultSet keys = con.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Fallo al upsertar propietario");
    }

    public static Propietario buscarPropietarioPorCedula(String cedula) {
        try (Connection con = getConexion()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Propietarios WHERE cedula = ?");
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Propietario(
                        rs.getInt("id"),
                        rs.getString("cedula"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getInt("tiene_ninos") == 1,
                        rs.getInt("hay_embarazadas") == 1,
                        rs.getInt("numero_embarazos_previos"),
                        rs.getInt("zona_rural") == 1
                );
            }
        } catch (SQLException ignore) {}
        return null;
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

    // NOTA DE SEGURIDAD:
    // Las contraseñas se guardan en texto plano en la base de datos SQLite.
    // Esto es válido para un proyecto académico, pero para un entorno de producción
    // se debe implementar una función de hashing criptográfico (ej. BCrypt, Argon2)
    // para asegurar las credenciales.
    public static boolean validarUsuario(String username, String password) {
        try (Connection con = getConexion()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Usuarios WHERE username = ? AND password = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Error al validar usuario: " + e.getMessage());
        }
        return false;
    }
}