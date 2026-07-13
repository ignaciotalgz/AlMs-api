package com.algz.alms.dtos.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
    @NotBlank(message = "El nombre de usuario es obligatorio") String nombre,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Formato de email invalido") String email,
    @NotBlank(message = "La contraseña es obligatoria") @Size(min = 7, message = "La contraseña debe tener al menos 7 caracteres") String password) {
        
}
