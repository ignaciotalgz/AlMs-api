package com.algz.alms.dtos.docenteasignatura;

import java.time.LocalDate;
import java.util.UUID;

import com.algz.alms.entidades.DocenteAsignatura;
import com.algz.alms.enumeraciones.Cargo;

public record DocenteAsignaturaResponseDTO(UUID docenteAsignaturaId, UUID docentePersonaId, String docenteLegajo, UUID asignaturaId, String asignaturaNombre, Cargo cargo, LocalDate fechaDesde, LocalDate fechaHasta, boolean baja, LocalDate fechaBaja) {
    public static DocenteAsignaturaResponseDTO of(DocenteAsignatura docenteAsignatura) {    
        return new DocenteAsignaturaResponseDTO(
                docenteAsignatura.getDocenteAsignaturaId(),
                docenteAsignatura.getDocente().getPersonaId(),
                docenteAsignatura.getDocente().getLegajo(),
                docenteAsignatura.getAsignatura().getAsignaturaId(),
                docenteAsignatura.getAsignatura().getNombre(),
                docenteAsignatura.getCargo(),
                docenteAsignatura.getFechaDesde(),
                docenteAsignatura.getFechaHasta(),
                docenteAsignatura.isBaja(),
                docenteAsignatura.getFechaBaja()
        );
    }
}
