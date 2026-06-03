package com.vetsentinel.repository;

public interface UsuarioRepository {
    boolean validarCredenciales(String username, String password);
}
