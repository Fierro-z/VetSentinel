package com.vetsentinel.repository;

import com.vetsentinel.model.Mascota;
import java.sql.SQLException;

public interface MascotaRepository {
    int upsert(Mascota mascota) throws SQLException;
}
