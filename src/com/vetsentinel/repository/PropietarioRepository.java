package com.vetsentinel.repository;

import com.vetsentinel.model.Propietario;
import java.sql.SQLException;

public interface PropietarioRepository {
    int upsert(Propietario propietario) throws SQLException;
    Propietario buscarPorCedula(String cedula);
}
