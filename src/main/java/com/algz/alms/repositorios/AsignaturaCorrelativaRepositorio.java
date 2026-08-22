package com.algz.alms.repositorios;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.AsignaturaCorrelativa;
import com.algz.alms.enumeraciones.TipoCorrelatividad;

@Repository
public interface AsignaturaCorrelativaRepositorio extends JpaRepository<AsignaturaCorrelativa, UUID> {
    List<AsignaturaCorrelativa> findByModuloAsignatura_ModuloAsignaturaId(UUID moduloAsignaturaId);
    boolean existsByModuloAsignatura_ModuloAsignaturaIdAndModuloAsignaturaRequerida_ModuloAsignaturaIdAndTipoCorrelatividad(UUID moduloAsignaturaId, UUID moduloAsignaturaRequeridaId, TipoCorrelatividad tipoCorrelatividad);   
}
