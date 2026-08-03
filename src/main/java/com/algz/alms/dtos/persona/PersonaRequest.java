package com.algz.alms.dtos.persona;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PersonaRequest(
    @NotBlank(message = "El documento es obligatorio") String documento, 
    @NotBlank(message = "Apellido es obligatorio") String apellidos, 
    @NotBlank(message = "Nombre es obligatorio") String nombres,
    @NotBlank(message = "El email es obligatorio") @Email(message = "Formato de email invalido") String email, 
    String telefono, 
    String domicilio) {

}
