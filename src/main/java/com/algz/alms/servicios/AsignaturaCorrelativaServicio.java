package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaRequestDTO;
import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaResponseDTO;
import com.algz.alms.entidades.AsignaturaCorrelativa;
import com.algz.alms.entidades.ModuloAsignatura;
import com.algz.alms.excepciones.AsignaturaCorrelativaNoEncontradaException;
import com.algz.alms.excepciones.ModuloAsignaturaNoEncontradaException;
import com.algz.alms.repositorios.AsignaturaCorrelativaRepositorio;
import com.algz.alms.repositorios.ModuloAsignaturaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignaturaCorrelativaServicio {
    private final AsignaturaCorrelativaRepositorio asignaturaCorrelativaRepositorio;
    private final ModuloAsignaturaRepositorio moduloAsignaturaRepositorio;

    @Transactional
    public AsignaturaCorrelativaResponseDTO crear(AsignaturaCorrelativaRequestDTO request) {
        if (request.moduloAsignaturaId().equals(request.moduloAsignaturaRequeridaId())) {
            throw new IllegalArgumentException("Una asignatura no puede ser correlativa de si misma");
        }

        ModuloAsignatura moduloAsignatura = moduloAsignaturaRepositorio.findById(request.moduloAsignaturaId())
                .orElseThrow(() -> new ModuloAsignaturaNoEncontradaException(
                        "No se encontro la relacion modulo-asignatura con id " + request.moduloAsignaturaId()));

        ModuloAsignatura moduloAsignaturaRequerida = moduloAsignaturaRepositorio
                .findById(request.moduloAsignaturaRequeridaId())
                .orElseThrow(() -> new ModuloAsignaturaNoEncontradaException(
                        "No se encontro la relacion modulo-asignatura con id "
                                + request.moduloAsignaturaRequeridaId()));

        if (asignaturaCorrelativaRepositorio
                .existsByModuloAsignatura_ModuloAsignaturaIdAndModuloAsignaturaRequerida_ModuloAsignaturaIdAndTipoCorrelatividad(
                        request.moduloAsignaturaId(), request.moduloAsignaturaRequeridaId(),
                        request.tipoCorrelatividad())) {
            throw new IllegalArgumentException("Esta correlatividad ya esta registrada");
        }

        AsignaturaCorrelativa correlativa = AsignaturaCorrelativa.builder()
                .moduloAsignatura(moduloAsignatura)
                .moduloAsignaturaRequerida(moduloAsignaturaRequerida)
                .tipoCorrelatividad(request.tipoCorrelatividad())
                .baja(false)
                .build();

        asignaturaCorrelativaRepositorio.save(correlativa);
        return AsignaturaCorrelativaResponseDTO.of(correlativa);
    }
    @Transactional(readOnly = true)
    public List<AsignaturaCorrelativaResponseDTO> listarPorModuloAsignatura(
            UUID moduloAsignaturaId, boolean incluirBajas) {
        if (!moduloAsignaturaRepositorio.existsById(moduloAsignaturaId)) {
            throw new ModuloAsignaturaNoEncontradaException(
                    "No se encontro la relacion modulo-asignatura con id " + moduloAsignaturaId);
        }
        return asignaturaCorrelativaRepositorio.findByModuloAsignatura_ModuloAsignaturaId(moduloAsignaturaId).stream()
                .filter(correlativa -> incluirBajas || !correlativa.isBaja())
                .map(AsignaturaCorrelativaResponseDTO::of)
                .toList();
    }
    @Transactional(readOnly = true)
    public AsignaturaCorrelativaResponseDTO obtenerPorId(UUID asignaturaCorrelativaId) {
        return AsignaturaCorrelativaResponseDTO.of(buscarPorIdOLanzar(asignaturaCorrelativaId));
    }

    @Transactional
    public void darDeBaja(UUID asignaturaCorrelativaId) {
        buscarPorIdOLanzar(asignaturaCorrelativaId).setBaja(true);
    }

    @Transactional
    public void reactivar(UUID asignaturaCorrelativaId) {
        buscarPorIdOLanzar(asignaturaCorrelativaId).setBaja(false);
    }
    private AsignaturaCorrelativa buscarPorIdOLanzar(UUID asignaturaCorrelativaId) {
        return asignaturaCorrelativaRepositorio.findById(asignaturaCorrelativaId)
                .orElseThrow(() -> new AsignaturaCorrelativaNoEncontradaException(
                        "No se encontro la correlatividad con id " + asignaturaCorrelativaId));
    }
}
