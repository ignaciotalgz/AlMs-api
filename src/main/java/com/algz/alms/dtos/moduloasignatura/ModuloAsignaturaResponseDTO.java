package com.algz.alms.dtos.moduloasignatura;

import java.util.UUID;

import com.algz.alms.entidades.ModuloAsignatura;

public record ModuloAsignaturaResponseDTO(UUID moduloAsignaturaId, UUID moduloId, String moduloNombre, UUID asignaturaId, String asignaturaNombre, String asignaturaCodigo, Integer orden, boolean baja) {
    public static ModuloAsignaturaResponseDTO of(ModuloAsignatura moduloAsignatura) {
        return new ModuloAsignaturaResponseDTO(
                moduloAsignatura.getModuloAsignaturaId(),
                moduloAsignatura.getModulo().getModuloId(),
                moduloAsignatura.getModulo().getNombre(),
                moduloAsignatura.getAsignatura().getAsignaturaId(),
                moduloAsignatura.getAsignatura().getNombre(),
                moduloAsignatura.getAsignatura().getCodigo(),
                moduloAsignatura.getOrden(),
                moduloAsignatura.isBaja()
        );
    }
}
