package com.algz.alms.dtos.docenteasignatura;

import java.time.LocalDate;
import java.util.UUID;

import com.algz.alms.enumeraciones.Cargo;

import jakarta.validation.constraints.NotNull;

public record DocenteAsignaturaRequestDTO(@NotNull(message = "El docente es obligatorio") UUID docenteId, @NotNull(message = "La asignatura es obligatoria") UUID asignaturaId, @NotNull(message = "El cargo es obligatorio") Cargo cargo, @NotNull(message = "La fecha desde es obligatoria") LocalDate fechaDesde, LocalDate fechaHasta) {

}
