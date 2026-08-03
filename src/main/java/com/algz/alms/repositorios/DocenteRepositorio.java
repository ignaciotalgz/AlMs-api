package com.algz.alms.repositorios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Docente;

@Repository
public interface DocenteRepositorio extends JpaRepository<Docente, UUID> {
@Query("select d from Docente d where d.persona.baja = false")
    List<Docente> findActivos();

    @Query("select d from Docente d where d.personaId = :personaId and d.persona.baja = false")
    Optional<Docente> findActivoPorPersonaId(@Param("personaId") UUID personaId);
}
