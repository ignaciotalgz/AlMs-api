package com.algz.alms.dtos.modulo;

import java.util.UUID;

import com.algz.alms.entidades.Modulo;

public record ModuloResponseDTO(UUID moduloId, UUID planEstudioId, String planEstudioNombre, String nombre, Integer orden, boolean baja) {
    public static ModuloResponseDTO of(Modulo modulo) {
        return new ModuloResponseDTO(
                modulo.getModuloId(),
                modulo.getPlanEstudio().getPlanEstudioId(),
                modulo.getPlanEstudio().getNombre(),
                modulo.getNombre(),
                modulo.getOrden(),
                modulo.isBaja()
        );
    }
}
