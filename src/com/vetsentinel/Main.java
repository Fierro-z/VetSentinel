package com.vetsentinel;

import com.vetsentinel.config.DatabaseConfig;
import com.vetsentinel.repository.*;
import com.vetsentinel.repository.impl.*;
import com.vetsentinel.service.*;
import com.vetsentinel.service.impl.*;
import com.vetsentinel.ui.VentanaSelector;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Inicializar configuración e inicialización de la base de datos
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.inicializarBD();

        // Pre-cargar la base cartográfica del mapa de forma asíncrona
        new Thread(() -> {
            com.vetsentinel.ui.PanelMapaColombia.preCargarMapa();
        }).start();

        // 2. Instanciar repositorios concretos
        PropietarioRepository propietarioRepo = new SQLitePropietarioRepository(dbConfig);
        MascotaRepository mascotaRepo = new SQLiteMascotaRepository(dbConfig);
        ParasitoRepository parasitoRepo = new SQLiteParasitoRepository(dbConfig);
        DiagnosticoRepository diagnosticoRepo = new SQLiteDiagnosticoRepository(dbConfig);
        UsuarioRepository usuarioRepo = new SQLiteUsuarioRepository(dbConfig);

        // 3. Ensamblar estrategias de cálculo de riesgo (Strategy Pattern)
        List<RiskStrategy> strategies = new ArrayList<>();
        strategies.add(new LeishmaniasisVisceralStrategy());
        strategies.add(new LeishmaniasisCutaneaStrategy());
        strategies.add(new ToxoplasmosisStrategy());
        strategies.add(new DefaultRiskStrategy());

        // 4. Instanciar servicios de negocio inyectando sus dependencias
        RiskAssessmentService riskService = new RiskAssessmentService(strategies);
        AuthenticationService authService = new AuthenticationServiceImpl(usuarioRepo); // ← CORREGIDO

        // 5. Iniciar la interfaz de usuario inyectando las dependencias necesarias
        javax.swing.SwingUtilities.invokeLater(() -> {
            VentanaSelector selector = new VentanaSelector(
                propietarioRepo,
                mascotaRepo,
                parasitoRepo,
                diagnosticoRepo,
                authService,
                riskService
            );
            selector.setVisible(true);
        });
    }
}
