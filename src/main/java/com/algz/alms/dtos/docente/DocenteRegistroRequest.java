package com.algz.alms.dtos.docente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocenteRegistroRequest(
    @NotBlank(message = "El token de invitacion es obligatorio") String token,
    @NotBlank(message = "El documento es obligatorio") String documento,
    @NotBlank(message = "Apellido es obligatorio") String apellidos,
    @NotBlank(message = "Nombre es obligatorio") String nombres,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Formato de email invalido") String email,
    String telefono,
    String domicilio,
    @NotBlank(message = "La contraseña es obligatoria") @Size(min = 7, message = "La contraseña debe tener al menos 7 caracteres") String password) {

}
