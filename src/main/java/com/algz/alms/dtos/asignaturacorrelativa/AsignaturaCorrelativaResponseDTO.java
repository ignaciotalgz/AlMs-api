package com.algz.alms.dtos.asignaturacorrelativa;

import java.util.UUID;

import com.algz.alms.entidades.AsignaturaCorrelativa;
import com.algz.alms.enumeraciones.TipoCorrelatividad;

public record AsignaturaCorrelativaResponseDTO(UUID asignaturaCorrelativaId, UUID moduloAsignaturaId, String asignaturaNombre, UUID moduloAsignaturaRequeridaId, String asignaturaRequeridaNombre, TipoCorrelatividad tipoCorrelatividad, boolean baja) {
    public static AsignaturaCorrelativaResponseDTO of(AsignaturaCorrelativa correlativa) {
        return new AsignaturaCorrelativaResponseDTO(
                correlativa.getAsignaturaCorrelativaId(),
                correlativa.getModuloAsignatura().getModuloAsignaturaId(),
                correlativa.getModuloAsignatura().getAsignatura().getNombre(),
                correlativa.getModuloAsignaturaRequerida().getModuloAsignaturaId(),
                correlativa.getModuloAsignaturaRequerida().getAsignatura().getNombre(),
                correlativa.getTipoCorrelatividad(),
                correlativa.isBaja()
        );
    }
}
