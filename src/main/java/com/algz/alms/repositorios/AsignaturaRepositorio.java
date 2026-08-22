package com.algz.alms.repositorios;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Asignatura;

@Repository
public interface AsignaturaRepositorio extends JpaRepository<Asignatura, UUID> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndAsignaturaIdNot(String codigo, UUID asignaturaId);
}
