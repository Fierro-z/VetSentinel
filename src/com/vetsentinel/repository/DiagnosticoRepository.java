package com.vetsentinel.repository;

import com.vetsentinel.model.Diagnostico;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DiagnosticoRepository {
    void registrar(int idMascota, int idParasito, String nivelRiesgo) throws SQLException;
    Object[][] obtenerHistorial();
    Map<String, String> obtenerRiesgoPorDepartamento();
    List<String[]> obtenerCepasPorUbicacion();
    int obtenerTotalMascotasEvaluadas();
    int obtenerTotalDiagnosticosCriticos();
    String obtenerParasitoPredominante();
}
