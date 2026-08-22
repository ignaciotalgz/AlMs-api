package com.algz.alms.dtos.moduloasignatura;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ModuloAsignaturaRequestDTO(@NotNull(message = "El modulo es obligatorio") UUID moduloId, @NotNull(message = "La asignatura es obligatoria") UUID asignaturaId, @NotNull(message = "El orden es obligatorio") @Positive(message = "El orden debe ser un numero positivo") Integer orden) {

}
