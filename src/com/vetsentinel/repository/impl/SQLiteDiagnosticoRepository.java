package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.repository.DiagnosticoRepository;
import com.vetsentinel.repository.MascotaRepository;
import com.vetsentinel.repository.PropietarioRepository;
// IMPORTACIONES AGREGADAS PARA CORREGIR LOS ERRORES DE COMPILACIÓN
import com.vetsentinel.repository.impl.SQLitePropietarioRepository;
import com.vetsentinel.repository.impl.SQLiteMascotaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteDiagnosticoRepository implements DiagnosticoRepository {

    private final DatabaseConfig dbConfig;

    public SQLiteDiagnosticoRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public void registrar(int idMascota, int idParasito, String nivelRiesgo, String reporte) throws SQLException {
        String sql = "INSERT INTO Diagnosticos (id_mascota, id_parasito, fecha, estado_contagio, nivel_riesgo, reporte) VALUES (?,?,date('now'),'Activo',?,?)";
        try (Connection con = dbConfig.getConnection();
             PreparedStatement psDiag = con.prepareStatement(sql)) {
            psDiag.setInt(1, idMascota);
            psDiag.setInt(2, idParasito);
            psDiag.setString(3, nivelRiesgo);
            psDiag.setString(4, reporte);
            psDiag.executeUpdate();
        }
    }

    @Override
    public void registrarCasoCompleto(
            Propietario propietario,
            Mascota mascota,
            int idParasito,
            String nivelRiesgo,
            String reporte) throws SQLException {

        dbConfig.iniciarTransaccion();

        try {

            PropietarioRepository propRepo =
                    new SQLitePropietarioRepository(dbConfig);

            int idPropietario =
                    propRepo.upsert(propietario);

            Propietario propietarioConId = new Propietario(
                    idPropietario,
                    propietario.getCedula(),
                    propietario.getNombre(),
                    propietario.getDireccion(),
                    propietario.getDepartamento(),
                    propietario.isTieneNinos(),
                    propietario.isHayEmbarazadas(),
                    propietario.getNumeroDeEmbarazosPrevios(),
                    propietario.isZonaRural(),
                    propietario.getEstrato(),
                    propietario.getRegimen(),
                    propietario.getAltitud()
            );

            Mascota mascotaParaGuardar = new Mascota(
                    mascota.getId(),
                    mascota.getNombre(),
                    mascota.getEspecie(),
                    mascota.getEdad(),
                    propietarioConId
            );

            MascotaRepository mascRepo =
                    new SQLiteMascotaRepository(dbConfig);

            int idMascota =
                    mascRepo.upsert(mascotaParaGuardar);

            registrar(
                    idMascota,
                    idParasito,
                    nivelRiesgo,
                    reporte
            );

            dbConfig.commitTransaccion();

        } catch (Exception e) {

            dbConfig.rollbackTransaccion();

            if (e instanceof SQLException) {
                throw (SQLException) e;
            }

            throw new SQLException(
                    "Error en la transacción del registro del caso clínico.",
                    e
            );
        }
    }

    @Override
    public Object[][] obtenerHistorial() {
        String sql =
                "SELECT d.fecha, d.nivel_riesgo, d.reporte, " +
                        "m.nombre AS mascota, m.especie, " +
                        "p.nombre AS propietario, p.cedula, p.direccion, p.departamento, " +
                        "par.nombre AS parasito " +
                        "FROM Diagnosticos d " +
                        "JOIN Mascotas m ON d.id_mascota = m.id " +
                        "JOIN Propietarios p ON m.id_propietario = p.id " +
                        "JOIN Parasitos par ON d.id_parasito = par.id " +
                        "ORDER BY d.fecha DESC";

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("fecha"),
                        rs.getString("nivel_riesgo") != null ? rs.getString("nivel_riesgo") : "N/A",
                        rs.getString("cedula") != null ? rs.getString("cedula") : "-",
                        rs.getString("propietario"),
                        (rs.getString("direccion") != null ? rs.getString("direccion") : "-") + " (" + (rs.getString("departamento") != null ? rs.getString("departamento") : "") + ")",
                        rs.getString("mascota"),
                        rs.getString("especie"),
                        rs.getString("parasito"),
                        rs.getString("reporte") != null ? rs.getString("reporte") : ""
                });
            }
            return rows.toArray(new Object[0][]);
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerHistorial", e);
            return new Object[0][];
        }
    }

    @Override
    public Map<String, String> obtenerRiesgoPorDepartamento() {
        Map<String, String> mapa = new HashMap<>();
        String sql = "SELECT p.departamento, " +
                "MAX(CASE d.nivel_riesgo " +
                "WHEN 'EMERGENCIA CRÍTICA' THEN 5 " +
                "WHEN 'EMERGENCIA CRITICA' THEN 5 " +
                "WHEN 'CRITICO' THEN 4 " +
                "WHEN 'CRÍTICO' THEN 4 " +
                "WHEN 'ALTO' THEN 3 " +
                "WHEN 'MEDIO' THEN 2 " +
                "WHEN 'MODERADO' THEN 2 " +
                "WHEN 'BAJO' THEN 1 ELSE 0 END) as max_risk " +
                "FROM Diagnosticos d " +
                "JOIN Mascotas m ON d.id_mascota = m.id " +
                "JOIN Propietarios p ON m.id_propietario = p.id " +
                "JOIN Parasitos par ON d.id_parasito = par.id " +
                "GROUP BY p.departamento";

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dep = rs.getString("departamento");
                int maxRisk = rs.getInt("max_risk");
                String riesgoStr = "BAJO";
                if (maxRisk == 5) riesgoStr = "EMERGENCIA CRÍTICA";
                else if (maxRisk == 4) riesgoStr = "CRITICO";
                else if (maxRisk == 3) riesgoStr = "ALTO";
                else if (maxRisk == 2) riesgoStr = "MEDIO";
                mapa.put(dep, riesgoStr);
            }
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerRiesgoPorDepartamento", e);
        }
        return mapa;
    }

    @Override
    public List<String[]> obtenerCepasPorUbicacion() {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT p.departamento, par.nombre as parasito, count(*) as casos, d.nivel_riesgo " +
                "FROM Diagnosticos d " +
                "JOIN Mascotas m ON d.id_mascota = m.id " +
                "JOIN Propietarios p ON m.id_propietario = p.id " +
                "JOIN Parasitos par ON d.id_parasito = par.id " +
                "GROUP BY p.departamento, par.nombre, d.nivel_riesgo " +
                "ORDER BY p.departamento";

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("departamento") != null ? rs.getString("departamento") : "N/A",
                        rs.getString("parasito"),
                        String.valueOf(rs.getInt("casos")),
                        rs.getString("nivel_riesgo")
                });
            }
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerCepasPorUbicacion", e);
        }
        return lista;
    }

    @Override
    public int obtenerTotalMascotasEvaluadas() {
        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Mascotas")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerTotalMascotasEvaluadas", e);
        }
        return 0;
    }

    @Override
    public int obtenerTotalDiagnosticosCriticos() {
        String sql = "SELECT COUNT(*) FROM Diagnosticos WHERE nivel_riesgo IN ('CRITICO', 'CRÍTICO', 'EMERGENCIA CRÍTICA', 'EMERGENCIA CRITICA')";
        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerTotalDiagnosticosCriticos", e);
        }
        return 0;
    }

    @Override
    public String obtenerParasitoPredominante() {
        String sql = "SELECT p.nombre, COUNT(d.id_parasito) as cant " +
                "FROM Diagnosticos d " +
                "JOIN Parasitos p ON d.id_parasito = p.id " +
                "GROUP BY p.nombre ORDER BY cant DESC LIMIT 1";

        try (Connection con = dbConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerParasitoPredominante", e);
        }
        return "N/A";
    }

    @Override
    public Object[][] obtenerHistorialPorDepartamento(String departamento) {
        String sql =
                "SELECT d.fecha, d.nivel_riesgo, " +
                        "m.nombre AS mascota, m.especie, " +
                        "p.nombre AS propietario, p.cedula, p.direccion, " +
                        "par.nombre AS parasito " +
                        "FROM Diagnosticos d " +
                        "JOIN Mascotas m ON d.id_mascota = m.id " +
                        "JOIN Propietarios p ON m.id_propietario = p.id " +
                        "JOIN Parasitos par ON d.id_parasito = par.id " +
                        "WHERE LOWER(p.departamento) = LOWER(?) " +
                        "ORDER BY d.fecha DESC";

        try (Connection con = dbConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, departamento);
            try (ResultSet rs = ps.executeQuery()) {
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("fecha"),
                            rs.getString("propietario"),
                            rs.getString("direccion") != null ? rs.getString("direccion") : "-",
                            rs.getString("mascota"),
                            rs.getString("especie"),
                            rs.getString("parasito"),
                            rs.getString("nivel_riesgo") != null ? rs.getString("nivel_riesgo") : "N/A"
                    });
                }
                return rows.toArray(new Object[0][]);
            }
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error en obtenerHistorialPorDepartamento", e);
            return new Object[0][];
        }
    }
}