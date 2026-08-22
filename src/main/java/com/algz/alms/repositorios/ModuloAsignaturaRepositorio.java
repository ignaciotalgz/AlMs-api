package com.algz.alms.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.ModuloAsignatura;

@Repository
public interface ModuloAsignaturaRepositorio extends JpaRepository<ModuloAsignatura, UUID> {
    List<ModuloAsignatura> findByModulo_ModuloId(UUID moduloId);
    boolean existsByModulo_ModuloIdAndOrden(UUID moduloId, Integer orden);
    boolean existsByModulo_ModuloIdAndOrdenAndModuloAsignaturaIdNot(UUID moduloId, Integer orden, UUID moduloAsignaturaId);
    boolean existsByModulo_ModuloIdAndAsignatura_AsignaturaId(UUID moduloId, UUID asignaturaId);
}
