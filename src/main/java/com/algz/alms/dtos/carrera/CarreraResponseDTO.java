package com.algz.alms.dtos.carrera;

import java.util.UUID;

import com.algz.alms.entidades.Carrera;

public record CarreraResponseDTO(UUID carreraId, String nombre, boolean baja) {
    public static CarreraResponseDTO of(Carrera carrera) {
        return new CarreraResponseDTO(carrera.getCarreraId(), carrera.getNombre(), carrera.isBaja());
    }
}
