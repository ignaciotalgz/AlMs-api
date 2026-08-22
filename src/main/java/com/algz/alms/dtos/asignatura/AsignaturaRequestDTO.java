package com.algz.alms.dtos.asignatura;

import jakarta.validation.constraints.NotBlank;

public record AsignaturaRequestDTO(@NotBlank(message = "El nombre de la asignatura es obligatorio") String nombre, @NotBlank(message = "El codigo de la asignatura es obligatorio") String codigo) {

}
