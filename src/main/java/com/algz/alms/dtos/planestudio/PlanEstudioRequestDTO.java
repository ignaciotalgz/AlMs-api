package com.algz.alms.dtos.planestudio;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanEstudioRequestDTO(@NotNull(message = "La carrera es obligatoria") UUID carreraId, @NotBlank(message = "El nombre del plan de estudio es obligatorio") String nombre, @NotNull(message = "La fecha de vigencia es obligatoria") LocalDate vigenciaDesde, @NotNull(message = "La fecha de vigencia es obligatoria") LocalDate vigenciaHasta) {

}
