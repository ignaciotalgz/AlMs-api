package com.algz.alms.dtos.asignatura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignaturaRequestDTO(@NotBlank(message = "El nombre de la asignatura es obligatorio") String nombre, @NotBlank(message = "El codigo de la asignatura es obligatorio") String codigo, @NotNull(message = "La carga horaria es obligatoria") @Positive(message = "La carga horaria debe ser un numero positivo") Integer cargaHoraria) {

}
