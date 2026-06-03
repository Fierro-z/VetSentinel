package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.model.Propietario;
import com.vetsentinel.repository.PropietarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLitePropietarioRepository implements PropietarioRepository {

    private final DatabaseConfig dbConfig;

    public SQLitePropietarioRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public int upsert(Propietario p) throws SQLException {
        try (Connection con = dbConfig.getConnection()) {
            PreparedStatement psCheck = con.prepareStatement("SELECT id FROM Propietarios WHERE cedula = ?");
            psCheck.setString(1, p.getCedula());
            ResultSet rs = psCheck.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Propietarios SET nombre = ?, direccion = ?, departamento = ?, tiene_ninos = ?, hay_embarazadas = ?, numero_embarazos_previos = ?, zona_rural = ?, estrato = ?, regimen = ?, altitud = ? WHERE id = ?");
                psUpd.setString(1, p.getNombre());
                psUpd.setString(2, p.getDireccion());
                psUpd.setString(3, p.getDepartamento());
                psUpd.setInt(4, p.isTieneNinos() ? 1 : 0);
                psUpd.setInt(5, p.isHayEmbarazadas() ? 1 : 0);
                psUpd.setInt(6, p.getNumeroDeEmbarazosPrevios());
                psUpd.setInt(7, p.isZonaRural() ? 1 : 0);
                psUpd.setInt(8, p.getEstrato());
                psUpd.setString(9, p.getRegimen());
                psUpd.setInt(10, p.getAltitud());
                psUpd.setInt(11, id);
                psUpd.executeUpdate();
                return id;
            } else {
                PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO Propietarios (cedula, nombre, direccion, departamento, tiene_ninos, hay_embarazadas, numero_embarazos_previos, zona_rural, estrato, regimen, altitud) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                psIns.setString(1, p.getCedula());
                psIns.setString(2, p.getNombre());
                psIns.setString(3, p.getDireccion());
                psIns.setString(4, p.getDepartamento());
                psIns.setInt(5, p.isTieneNinos() ? 1 : 0);
                psIns.setInt(6, p.isHayEmbarazadas() ? 1 : 0);
                psIns.setInt(7, p.getNumeroDeEmbarazosPrevios());
                psIns.setInt(8, p.isZonaRural() ? 1 : 0);
                psIns.setInt(9, p.getEstrato());
                psIns.setString(10, p.getRegimen());
                psIns.setInt(11, p.getAltitud());
                psIns.executeUpdate();
                ResultSet keys = con.createStatement().executeQuery("SELECT last_insert_rowid()");
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Fallo al guardar propietario en la base de datos.");
    }

    @Override
    public Propietario buscarPorCedula(String cedula) {
        try (Connection con = dbConfig.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Propietarios WHERE cedula = ?");
            ps.setString(1, cedula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Propietario(
                        rs.getInt("id"),
                        rs.getString("cedula"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getString("departamento"),
                        rs.getInt("tiene_ninos") == 1,
                        rs.getInt("hay_embarazadas") == 1,
                        rs.getInt("numero_embarazos_previos"),
                        rs.getInt("zona_rural") == 1,
                        rs.getInt("estrato"),
                        rs.getString("regimen"),
                        rs.getInt("altitud")
                );
            }
        } catch (SQLException ignore) {}
        return null;
    }
}
