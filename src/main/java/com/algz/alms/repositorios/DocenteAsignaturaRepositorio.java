package com.algz.alms.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.DocenteAsignatura;
import com.algz.alms.enumeraciones.Cargo;

@Repository
public interface DocenteAsignaturaRepositorio extends JpaRepository<DocenteAsignatura, UUID> {
    List<DocenteAsignatura> findByAsignatura_AsignaturaId(UUID asignaturaId);

    List<DocenteAsignatura> findByDocente_PersonaId(UUID personaId);

    boolean existsByDocente_PersonaIdAndAsignatura_AsignaturaIdAndCargoAndBajaFalse(
            UUID personaId, UUID asignaturaId, Cargo cargo);
}
