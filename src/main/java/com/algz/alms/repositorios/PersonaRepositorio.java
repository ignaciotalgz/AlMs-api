package com.algz.alms.repositorios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Persona;

@Repository
public interface PersonaRepositorio extends JpaRepository<Persona, UUID> {
    List<Persona> findByBajaFalse();
    List<Persona> findByBajaTrue();
    Optional<Persona> findByPersonaIdAndBajaFalse(UUID personaId);
    Optional<Persona> findByDocumento(String documento);
    Optional<Persona> findByNombres(String nombres);
    Optional<Persona> findByApellidos(String apellidos);
    boolean existsByDocumento(String documento);
}
