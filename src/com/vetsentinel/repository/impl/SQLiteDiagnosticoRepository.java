package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.repository.DiagnosticoRepository;

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
    public void registrar(int idMascota, int idParasito, String nivelRiesgo) throws SQLException {
        try (Connection con = dbConfig.getConnection()) {
            PreparedStatement psDiag = con.prepareStatement(
                    "INSERT INTO Diagnosticos (id_mascota, id_parasito, fecha, estado_contagio, nivel_riesgo) VALUES (?,?,date('now'),'Activo',?)");
            psDiag.setInt(1, idMascota);
            psDiag.setInt(2, idParasito);
            psDiag.setString(3, nivelRiesgo);
            psDiag.executeUpdate();
        }
    }

    @Override
    public Object[][] obtenerHistorial() {
        try (Connection con = dbConfig.getConnection()) {
            String sql =
                    "SELECT d.fecha, d.nivel_riesgo, " +
                            "m.nombre AS mascota, m.especie, " +
                            "p.nombre AS propietario, p.cedula, p.direccion, p.departamento, " +
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
                        (rs.getString("direccion") != null ? rs.getString("direccion") : "-") + " (" + (rs.getString("departamento") != null ? rs.getString("departamento") : "") + ")",
                        rs.getString("mascota"),
                        rs.getString("especie"),
                        rs.getString("parasito")
                });
            }
            return rows.toArray(new Object[0][]);
        } catch (SQLException e) {
            System.err.println("Error en obtenerHistorial: " + e.getMessage());
            return new Object[0][];
        }
    }

    @Override
    public Map<String, String> obtenerRiesgoPorDepartamento() {
        Map<String, String> mapa = new HashMap<>();
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
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
            ResultSet rs = stmt.executeQuery(sql);
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
            System.err.println("Error en obtenerRiesgoPorDepartamento: " + e.getMessage());
        }
        return mapa;
    }

    @Override
    public List<String[]> obtenerCepasPorUbicacion() {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
            String sql = "SELECT p.departamento, par.nombre as parasito, count(*) as casos, d.nivel_riesgo " +
                         "FROM Diagnosticos d " +
                         "JOIN Mascotas m ON d.id_mascota = m.id " +
                         "JOIN Propietarios p ON m.id_propietario = p.id " +
                         "JOIN Parasitos par ON d.id_parasito = par.id " +
                         "GROUP BY p.departamento, par.nombre, d.nivel_riesgo " +
                         "ORDER BY p.departamento";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("departamento") != null ? rs.getString("departamento") : "N/A",
                        rs.getString("parasito"),
                        String.valueOf(rs.getInt("casos")),
                        rs.getString("nivel_riesgo")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerCepasPorUbicacion: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public int obtenerTotalMascotasEvaluadas() {
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Mascotas");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignore) {}
        return 0;
    }

    @Override
    public int obtenerTotalDiagnosticosCriticos() {
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Diagnosticos WHERE nivel_riesgo IN ('CRITICO', 'CRÍTICO', 'EMERGENCIA CRÍTICA', 'EMERGENCIA CRITICA')");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ignore) {}
        return 0;
    }

    @Override
    public String obtenerParasitoPredominante() {
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
            String sql = "SELECT p.nombre, COUNT(d.id_parasito) as cant " +
                         "FROM Diagnosticos d " +
                         "JOIN Parasitos p ON d.id_parasito = p.id " +
                         "GROUP BY p.nombre ORDER BY cant DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignore) {}
        return "N/A";
    }

    @Override
    public Object[][] obtenerHistorialPorDepartamento(String departamento) {
        try (Connection con = dbConfig.getConnection()) {
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

            try (PreparedStatement ps = con.prepareStatement(sql)) {
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
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerHistorialPorDepartamento: " + e.getMessage());
            return new Object[0][];
        }
    }

    @Override
    public void registrarCasoCompleto(com.vetsentinel.model.Propietario propietario, com.vetsentinel.model.Mascota mascota, int idParasito, String nivelRiesgo) throws SQLException {
        dbConfig.iniciarTransaccion();
        try {
            SQLitePropietarioRepository propRepo = new SQLitePropietarioRepository(dbConfig);
            int idProp = propRepo.upsert(propietario);
            
            mascota.getPropietario().setId(idProp);
            SQLiteMascotaRepository mascRepo = new SQLiteMascotaRepository(dbConfig);
            int idMasc = mascRepo.upsert(mascota);
            
            registrar(idMasc, idParasito, nivelRiesgo);
            
            dbConfig.commitTransaccion();
        } catch (Exception e) {
            dbConfig.rollbackTransaccion();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new SQLException("Error en la transacción del registro del caso clínico.", e);
            }
        }
    }
}
