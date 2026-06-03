package com.vetsentinel.repository.impl;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.model.Parasito;
import com.vetsentinel.repository.ParasitoRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteParasitoRepository implements ParasitoRepository {

    private final DatabaseConfig dbConfig;

    public SQLiteParasitoRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public List<Parasito> obtenerTodos() {
        List<Parasito> lista = new ArrayList<>();
        try (Connection con = dbConfig.getConnection(); Statement stmt = con.createStatement()) {
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
            System.err.println("Error al obtener parásitos: " + e.getMessage());
        }
        return lista;
    }
}
