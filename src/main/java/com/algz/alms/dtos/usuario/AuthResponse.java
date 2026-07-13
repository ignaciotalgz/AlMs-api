package com.algz.alms.dtos.usuario;

public record AuthResponse(String token, String tipo, String nombre, String email, String rol) {
    public static AuthResponse of(String token, String nombre, String email, String rol){
        return new AuthResponse(token, "Bearer", nombre, email, rol);
    }
}
