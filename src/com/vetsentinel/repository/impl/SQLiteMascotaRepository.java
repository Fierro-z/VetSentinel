package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.model.Mascota;
import com.vetsentinel.repository.MascotaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteMascotaRepository implements MascotaRepository {

    private final DatabaseConfig dbConfig;

    public SQLiteMascotaRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public int upsert(Mascota m) throws SQLException {
        try (Connection con = dbConfig.getConnection()) {
            int id = -1;
            try (PreparedStatement psCheck = con.prepareStatement(
                    "SELECT id FROM Mascotas WHERE nombre = ? AND id_propietario = ?")) {
                psCheck.setString(1, m.getNombre());
                psCheck.setInt(2, m.getPropietario().getId());
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            }

            if (id != -1) {
                try (PreparedStatement psUpd = con.prepareStatement(
                        "UPDATE Mascotas SET especie = ?, edad = ? WHERE id = ?")) {
                    psUpd.setString(1, m.getEspecie());
                    psUpd.setInt(2, m.getEdad());
                    psUpd.setInt(3, id);
                    psUpd.executeUpdate();
                    return id;
                }
            } else {
                try (PreparedStatement psIns = con.prepareStatement(
                        "INSERT OR REPLACE INTO Mascotas (nombre, especie, edad, id_propietario) VALUES (?, ?, ?, ?)")) {
                    psIns.setString(1, m.getNombre());
                    psIns.setString(2, m.getEspecie());
                    psIns.setInt(3, m.getEdad());
                    psIns.setInt(4, m.getPropietario().getId());
                    psIns.executeUpdate();
                    try (java.sql.Statement keysStmt = con.createStatement();
                         ResultSet keys = keysStmt.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Fallo al guardar mascota en la base de datos.");
    }
}
