package com.vetsentinel.service.impl;

import com.vetsentinel.repository.UsuarioRepository;
import com.vetsentinel.service.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final java.util.Map<String, Integer> intentosFallidos = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Long> bloqueos = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_INTENTOS = 3;
    private static final long TIEMPO_BLOQUEO_MS = 30000; // 30 segundos

    public AuthenticationServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        
        String cleanUser = username.trim().toLowerCase();

        // Verificar si está bloqueado
        if (bloqueos.containsKey(cleanUser)) {
            long tiempoRestante = bloqueos.get(cleanUser) - System.currentTimeMillis();
            if (tiempoRestante > 0) {
                com.vetsentinel.util.VetLogger.warn("Intento de login para usuario '" + username + "' bloqueado temporalmente. Restan " + (tiempoRestante / 1000) + "s.");
                throw new SecurityException("Usuario bloqueado temporalmente. Por favor, espera " + (tiempoRestante / 1000 + 1) + " segundos.");
            } else {
                bloqueos.remove(cleanUser);
                intentosFallidos.put(cleanUser, 0);
            }
        }

        boolean success = usuarioRepository.validarCredenciales(username.trim(), password);

        if (success) {
            intentosFallidos.remove(cleanUser);
            bloqueos.remove(cleanUser);
        } else {
            int intentos = intentosFallidos.getOrDefault(cleanUser, 0) + 1;
            intentosFallidos.put(cleanUser, intentos);
            com.vetsentinel.util.VetLogger.warn("Intento de login fallido para usuario '" + username + "'. Intento #" + intentos);

            if (intentos >= MAX_INTENTOS) {
                bloqueos.put(cleanUser, System.currentTimeMillis() + TIEMPO_BLOQUEO_MS);
                com.vetsentinel.util.VetLogger.error("Usuario '" + username + "' bloqueado por superar el limite de " + MAX_INTENTOS + " intentos.");
                throw new SecurityException("Usuario bloqueado por superar el límite de " + MAX_INTENTOS + " intentos fallidos. Intenta de nuevo en 30 segundos.");
            }
        }

        return success;
    }
}
