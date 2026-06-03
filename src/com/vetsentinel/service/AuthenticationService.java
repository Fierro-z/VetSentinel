package com.vetsentinel.service;

import com.vetsentinel.repository.UsuarioRepository;

public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        return usuarioRepository.validarCredenciales(username.trim(), password);
    }
}
