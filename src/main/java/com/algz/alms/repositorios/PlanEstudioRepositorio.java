package com.algz.alms.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.PlanEstudio;

@Repository
public interface PlanEstudioRepositorio extends JpaRepository<PlanEstudio, UUID>{
    List<PlanEstudio> findByCarrera_CarreraId(UUID carreraId);
    boolean existsByCarrera_CarreraIdAndNombreIgnoreCase(UUID carreraId, String nombre);
}
