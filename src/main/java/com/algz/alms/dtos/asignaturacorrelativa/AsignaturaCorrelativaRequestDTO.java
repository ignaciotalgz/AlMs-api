package com.algz.alms.dtos.asignaturacorrelativa;

import java.util.UUID;

import com.algz.alms.enumeraciones.TipoCorrelatividad;

import jakarta.validation.constraints.NotNull;

public record AsignaturaCorrelativaRequestDTO(@NotNull(message = "La asignatura que exige la correlatividad es obligatoria") UUID moduloAsignaturaId, @NotNull(message = "La asignatura requerida es obligatoria") UUID moduloAsignaturaRequeridaId, @NotNull(message = "El tipo de correlatividad es obligatorio") TipoCorrelatividad tipoCorrelatividad) {

}
