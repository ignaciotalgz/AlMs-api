package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.modulo.ModuloRequestDTO;
import com.algz.alms.dtos.modulo.ModuloResponseDTO;
import com.algz.alms.entidades.Modulo;
import com.algz.alms.entidades.PlanEstudio;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.repositorios.ModuloRepositorio;
import com.algz.alms.repositorios.PlanEstudioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuloServicio {
    private final ModuloRepositorio moduloRepositorio;
    private final PlanEstudioRepositorio planEstudioRepositorio;

    @Transactional
    public ModuloResponseDTO crear(ModuloRequestDTO request) {
        PlanEstudio planEstudio = planEstudioRepositorio.findById(request.planEstudioId())
                .orElseThrow(() -> new PlanEstudioNoEncontradoException(
                        "No se encontro el plan de estudio con id " + request.planEstudioId()));

        if (moduloRepositorio.existsByPlanEstudio_PlanEstudioIdAndOrden(request.planEstudioId(), request.orden())) {
            throw new IllegalArgumentException(
                    "Ya existe un modulo con el orden " + request.orden() + " en este plan de estudio");
        }

        Modulo modulo = Modulo.builder()
                .planEstudio(planEstudio)
                .nombre(request.nombre())
                .orden(request.orden())
                .baja(false)
                .build();

        moduloRepositorio.save(modulo);
        return ModuloResponseDTO.of(modulo);
    }
    @Transactional(readOnly = true)
    public List<ModuloResponseDTO> listarPorPlan(UUID planEstudioId, boolean incluirBajas) {
        if (!planEstudioRepositorio.existsById(planEstudioId)) {
            throw new PlanEstudioNoEncontradoException("No se encontro el plan de estudio con id " + planEstudioId);
        }
        return moduloRepositorio.findByPlanEstudio_PlanEstudioId(planEstudioId).stream()
                .filter(modulo -> incluirBajas || !modulo.isBaja())
                .map(ModuloResponseDTO::of)
                .toList();
    }
    @Transactional(readOnly = true)
    public ModuloResponseDTO obtenerPorId(UUID moduloId) {
        return ModuloResponseDTO.of(buscarPorIdOLanzar(moduloId));
    }

    @Transactional
    public ModuloResponseDTO actualizar(UUID moduloId, ModuloRequestDTO request) {
        Modulo modulo = buscarPorIdOLanzar(moduloId);

        PlanEstudio planEstudio = planEstudioRepositorio.findById(request.planEstudioId())
                .orElseThrow(() -> new PlanEstudioNoEncontradoException(
                        "No se encontro el plan de estudio con id " + request.planEstudioId()));

        if (moduloRepositorio.existsByPlanEstudio_PlanEstudioIdAndOrdenAndModuloIdNot(
                request.planEstudioId(), request.orden(), moduloId)) {
            throw new IllegalArgumentException(
                    "Ya existe un modulo con el orden " + request.orden() + " en este plan de estudio");
        }

        modulo.setPlanEstudio(planEstudio);
        modulo.setNombre(request.nombre());
        modulo.setOrden(request.orden());

        return ModuloResponseDTO.of(modulo);
    }
    @Transactional
    public void darDeBaja(UUID moduloId) {
        buscarPorIdOLanzar(moduloId).setBaja(true);
    }

    @Transactional
    public void reactivar(UUID moduloId) {
        buscarPorIdOLanzar(moduloId).setBaja(false);
    }

    private Modulo buscarPorIdOLanzar(UUID moduloId) {
        return moduloRepositorio.findById(moduloId)
                .orElseThrow(() -> new ModuloNoEncontradoException("No se encontro el modulo con id " + moduloId));
    }
}
