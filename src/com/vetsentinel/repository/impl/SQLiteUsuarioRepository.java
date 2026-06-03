package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.repository.UsuarioRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteUsuarioRepository implements UsuarioRepository {

    private final DatabaseConfig dbConfig;

    public SQLiteUsuarioRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public boolean validarCredenciales(String username, String password) {
        try (Connection con = dbConfig.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT password FROM Usuarios WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return com.vetsentinel.util.PasswordHasher.verify(password, storedHash);
                }
            }
        } catch (SQLException e) {
            com.vetsentinel.util.VetLogger.error("Error al validar usuario", e);
        }
        return false;
    }
}
