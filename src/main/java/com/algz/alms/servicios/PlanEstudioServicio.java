package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.planestudio.PlanEstudioRequestDTO;
import com.algz.alms.dtos.planestudio.PlanEstudioResponseDTO;
import com.algz.alms.entidades.Carrera;
import com.algz.alms.entidades.PlanEstudio;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.repositorios.CarreraRepositorio;
import com.algz.alms.repositorios.PlanEstudioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanEstudioServicio {
    private final PlanEstudioRepositorio planEstudioRepositorio;
    private final CarreraRepositorio carreraRepositorio;

    @Transactional
    public PlanEstudioResponseDTO crear(PlanEstudioRequestDTO request) {
        Carrera carrera = carreraRepositorio.findById(request.carreraId())
                .orElseThrow(() -> new CarreraNoEncontradaException(
                        "No se encontró la carrera con id " + request.carreraId()));

        if (planEstudioRepositorio.existsByCarrera_CarreraIdAndNombreIgnoreCase(
                request.carreraId(), request.nombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un plan de estudio con ese nombre para esta carrera");
        }

        PlanEstudio planEstudio = PlanEstudio.builder()
                .carrera(carrera)
                .nombre(request.nombre())
                .vigenciaDesde(request.vigenciaDesde())
                .vigenciaHasta(request.vigenciaHasta())
                .baja(false)
                .build();

        planEstudioRepositorio.save(planEstudio);
        return PlanEstudioResponseDTO.of(planEstudio);
    }

    @Transactional(readOnly = true)
    public List<PlanEstudioResponseDTO> listarTodos(boolean incluirBajas) {
        return planEstudioRepositorio.findAll().stream()
                .filter(plan -> incluirBajas || !plan.isBaja())
                .map(PlanEstudioResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlanEstudioResponseDTO> listarPorCarrera(UUID carreraId, boolean incluirBajas) {
        if (!carreraRepositorio.existsById(carreraId)) {
            throw new CarreraNoEncontradaException("No se encontró la carrera con id " + carreraId);
        }
        return planEstudioRepositorio.findByCarrera_CarreraId(carreraId).stream()
                .filter(plan -> incluirBajas || !plan.isBaja())
                .map(PlanEstudioResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanEstudioResponseDTO obtenerPorId(UUID planEstudioId) {
        return PlanEstudioResponseDTO.of(buscarPorIdOLanzar(planEstudioId));
    }

    @Transactional
    public PlanEstudioResponseDTO actualizar(UUID planEstudioId, PlanEstudioRequestDTO request) {
        PlanEstudio planEstudio = buscarPorIdOLanzar(planEstudioId);

        Carrera carrera = carreraRepositorio.findById(request.carreraId())
                .orElseThrow(() -> new CarreraNoEncontradaException(
                        "No se encontró la carrera con id " + request.carreraId()));

        planEstudio.setCarrera(carrera);
        planEstudio.setNombre(request.nombre());
        planEstudio.setVigenciaDesde(request.vigenciaDesde());
        planEstudio.setVigenciaHasta(request.vigenciaHasta());

        return PlanEstudioResponseDTO.of(planEstudio);
    }

    @Transactional
    public void darDeBaja(UUID planEstudioId) {
        buscarPorIdOLanzar(planEstudioId).setBaja(true);
    }

    @Transactional
    public void reactivar(UUID planEstudioId) {
        buscarPorIdOLanzar(planEstudioId).setBaja(false);
    }

    private PlanEstudio buscarPorIdOLanzar(UUID planEstudioId) {
        return planEstudioRepositorio.findById(planEstudioId)
                .orElseThrow(() -> new PlanEstudioNoEncontradoException(
                        "No se encontró el plan de estudio con id " + planEstudioId));
    }
}
