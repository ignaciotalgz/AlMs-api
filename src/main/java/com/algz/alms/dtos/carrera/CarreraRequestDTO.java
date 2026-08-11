package com.algz.alms.dtos.carrera;

import jakarta.validation.constraints.NotBlank;

public record CarreraRequestDTO(@NotBlank(message = "El nombre de la carrera es obligatorio") String nombre) {

}
