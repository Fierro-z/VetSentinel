import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VeterinariaDAO {

    public static List<Parasito> obtenerTodosLosParasitos() {
        List<Parasito> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConexion(); Statement stmt = con.createStatement()) {
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

    public static Object[][] obtenerHistorial() {
        try (Connection con = ConexionDB.getConexion()) {
            String sql =
                    "SELECT d.fecha, d.nivel_riesgo, " +
                            "m.nombre AS mascota, m.especie, " +
                            "p.nombre AS propietario, p.cedula, p.direccion, " +
                            "par.nombre AS parasito " +
                            "FROM Diagnosticos d " +
                            "JOIN Mascotas m ON d.id_mascota = m.id " +
                            "JOIN Propietarios p ON m.id_propietario = p.id " +
                            "JOIN Parasitos par ON d.id_parasito = par.id " +
                            "ORDER BY d.fecha DESC";

            Statement stmt = con.createStatement();
            ResultSet rs   = stmt.executeQuery(sql);

            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("fecha"),
                        rs.getString("nivel_riesgo") != null ? rs.getString("nivel_riesgo") : "N/A",
                        rs.getString("cedula") != null ? rs.getString("cedula") : "-",
                        rs.getString("propietario"),
                        rs.getString("direccion") != null ? rs.getString("direccion") : "-",
                        rs.getString("mascota"),
                        rs.getString("especie"),
                        rs.getString("parasito")
                });
            }
            return rows.toArray(new Object[0][]);
        } catch (SQLException e) {
            System.out.println("Error en obtenerHistorial: " + e.getMessage());
            return new Object[0][];
        }
    }

    public static String obtenerEstadisticasEpidemiologicas() {
        StringBuilder stats = new StringBuilder();
        try (Connection con = ConexionDB.getConexion(); Statement stmt = con.createStatement()) {
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
        try (Connection con = ConexionDB.getConexion()) {
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
        try (Connection con = ConexionDB.getConexion()) {
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
        try (Connection con = ConexionDB.getConexion()) {
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
        try (Connection con = ConexionDB.getConexion()) {
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
        try (Connection con = ConexionDB.getConexion()) {
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
