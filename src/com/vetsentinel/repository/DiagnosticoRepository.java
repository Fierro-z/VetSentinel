package com.vetsentinel.repository;

import com.vetsentinel.model.Diagnostico;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DiagnosticoRepository {
    void registrar(int idMascota, int idParasito, String nivelRiesgo, String reporte) throws SQLException;
    void registrarCasoCompleto(com.vetsentinel.model.Propietario propietario, com.vetsentinel.model.Mascota mascota, int idParasito, String nivelRiesgo, String reporte) throws SQLException;
    Object[][] obtenerHistorial();
    Object[][] obtenerHistorialPorDepartamento(String departamento);
    Map<String, String> obtenerRiesgoPorDepartamento();
    List<String[]> obtenerCepasPorUbicacion();
    int obtenerTotalMascotasEvaluadas();
    int obtenerTotalDiagnosticosCriticos();
    String obtenerParasitoPredominante();
}
