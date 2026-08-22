package com.algz.alms.dtos.modulo;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModuloRequestDTO(@NotNull(message = "El plan de estudio es obligatorio") UUID planEstudioId, @NotBlank(message = "El nombre del modulo es obligatorio") String nombre, @NotNull(message = "El orden es obligatorio") @Positive(message = "El orden debe ser un numero positivo") Integer orden) {

}
