package com.algz.alms.dtos.planestudio;

import java.time.LocalDate;
import java.util.UUID;

import com.algz.alms.entidades.PlanEstudio;

public record PlanEstudioResponseDTO(UUID planEstudioId, UUID carreraId, String carreraNombre, String nombre, LocalDate vigenciaDesde, LocalDate vigenciaHasta, boolean baja) {
    public static PlanEstudioResponseDTO of(PlanEstudio planEstudio) {
        return new PlanEstudioResponseDTO(planEstudio.getPlanEstudioId(), planEstudio.getCarrera().getCarreraId(), planEstudio.getCarrera().getNombre(), planEstudio.getNombre(), planEstudio.getVigenciaDesde(), planEstudio.getVigenciaHasta(), planEstudio.isBaja()
        );
    }
}
