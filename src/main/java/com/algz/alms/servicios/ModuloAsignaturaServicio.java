package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaRequestDTO;
import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaResponseDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.entidades.Modulo;
import com.algz.alms.entidades.ModuloAsignatura;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.excepciones.ModuloAsignaturaNoEncontradaException;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.repositorios.AsignaturaRepositorio;
import com.algz.alms.repositorios.ModuloAsignaturaRepositorio;
import com.algz.alms.repositorios.ModuloRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuloAsignaturaServicio {
    private final ModuloAsignaturaRepositorio moduloAsignaturaRepositorio;
    private final ModuloRepositorio moduloRepositorio;
    private final AsignaturaRepositorio asignaturaRepositorio;

    @Transactional
    public ModuloAsignaturaResponseDTO crear(ModuloAsignaturaRequestDTO request) {
        Modulo modulo = moduloRepositorio.findById(request.moduloId())
                .orElseThrow(() -> new ModuloNoEncontradoException(
                        "No se encontro el modulo con id " + request.moduloId()));

        Asignatura asignatura = asignaturaRepositorio.findById(request.asignaturaId())
                .orElseThrow(() -> new AsignaturaNoEncontradaException(
                        "No se encontro la asignatura con id " + request.asignaturaId()));

        if (moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndAsignatura_AsignaturaId(
                request.moduloId(), request.asignaturaId())) {
            throw new IllegalArgumentException("Esta asignatura ya esta agregada a este modulo");
        }
        if (moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndOrden(request.moduloId(), request.orden())) {
            throw new IllegalArgumentException(
                    "Ya existe una asignatura con el orden " + request.orden() + " en este modulo");
        }

        ModuloAsignatura moduloAsignatura = ModuloAsignatura.builder()
                .modulo(modulo)
                .asignatura(asignatura)
                .orden(request.orden())
                .baja(false)
                .build();

        moduloAsignaturaRepositorio.save(moduloAsignatura);
        return ModuloAsignaturaResponseDTO.of(moduloAsignatura);
    }
    @Transactional(readOnly = true)
    public List<ModuloAsignaturaResponseDTO> listarPorModulo(UUID moduloId, boolean incluirBajas) {
        if (!moduloRepositorio.existsById(moduloId)) {
            throw new ModuloNoEncontradoException("No se encontro el modulo con id " + moduloId);
        }
        return moduloAsignaturaRepositorio.findByModulo_ModuloId(moduloId).stream()
                .filter(moduloAsignatura -> incluirBajas || !moduloAsignatura.isBaja())
                .map(ModuloAsignaturaResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModuloAsignaturaResponseDTO obtenerPorId(UUID moduloAsignaturaId) {
        return ModuloAsignaturaResponseDTO.of(buscarPorIdOLanzar(moduloAsignaturaId));
    }
    @Transactional
    public ModuloAsignaturaResponseDTO actualizarOrden(UUID moduloAsignaturaId, Integer nuevoOrden) {
        ModuloAsignatura moduloAsignatura = buscarPorIdOLanzar(moduloAsignaturaId);

        if (moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndOrdenAndModuloAsignaturaIdNot(
                moduloAsignatura.getModulo().getModuloId(), nuevoOrden, moduloAsignaturaId)) {
            throw new IllegalArgumentException(
                    "Ya existe una asignatura con el orden " + nuevoOrden + " en este modulo");
        }

        moduloAsignatura.setOrden(nuevoOrden);
        return ModuloAsignaturaResponseDTO.of(moduloAsignatura);
    }

    @Transactional
    public void darDeBaja(UUID moduloAsignaturaId) {
        buscarPorIdOLanzar(moduloAsignaturaId).setBaja(true);
    }
    @Transactional
    public void reactivar(UUID moduloAsignaturaId) {
        buscarPorIdOLanzar(moduloAsignaturaId).setBaja(false);
    }

    private ModuloAsignatura buscarPorIdOLanzar(UUID moduloAsignaturaId) {
        return moduloAsignaturaRepositorio.findById(moduloAsignaturaId)
                .orElseThrow(() -> new ModuloAsignaturaNoEncontradaException(
                        "No se encontro la relacion modulo-asignatura con id " + moduloAsignaturaId));
    }
}
