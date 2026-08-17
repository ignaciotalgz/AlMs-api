package com.algz.alms.repositorios;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Carrera;

@Repository
public interface CarreraRepositorio extends JpaRepository<Carrera, UUID>{
    boolean existsByNombreIgnoreCase(String nombre);
}
