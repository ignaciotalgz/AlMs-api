package com.algz.alms.repositorios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Alumno;

@Repository
public interface AlumnoRepositorio extends JpaRepository<Alumno, UUID>{
@Query("select a from Alumno a where a.persona.baja = false")
    List<Alumno> findActivos();

    @Query("select a from Alumno a where a.personaId = :personaId and a.persona.baja = false")
    Optional<Alumno> findActivoPorPersonaId(@Param("personaId") UUID personaId);
}
