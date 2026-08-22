package com.algz.alms.dtos.asignatura;

import java.util.UUID;

import com.algz.alms.entidades.Asignatura;

public record AsignaturaResponseDTO(UUID asignaturaId, String nombre, String codigo, boolean baja) {
    public static AsignaturaResponseDTO of(Asignatura asignatura) {
        return new AsignaturaResponseDTO(
                asignatura.getAsignaturaId(),
                asignatura.getNombre(),
                asignatura.getCodigo(),
                asignatura.isBaja()
        );
    }
}
