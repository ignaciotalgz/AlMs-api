package com.algz.alms.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Modulo;
@Repository
public interface ModuloRepositorio extends JpaRepository<Modulo, UUID> {
    List<Modulo> findByPlanEstudio_PlanEstudioId(UUID planEstudioId);
    boolean existsByPlanEstudio_PlanEstudioIdAndOrden(UUID planEstudioId, Integer orden);
    boolean existsByPlanEstudio_PlanEstudioIdAndOrdenAndModuloIdNot(UUID planEstudioId, Integer orden, UUID moduloId);
}
