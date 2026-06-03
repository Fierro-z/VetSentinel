package com.vetsentinel.service.impl;

import com.vetsentinel.repository.UsuarioRepository;
import com.vetsentinel.service.AuthenticationService;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsuarioRepository usuarioRepository;

    public AuthenticationServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return usuarioRepository.validarCredenciales(username.trim(), password);
    }
}
