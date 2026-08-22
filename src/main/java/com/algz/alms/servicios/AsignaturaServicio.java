package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.asignatura.AsignaturaRequestDTO;
import com.algz.alms.dtos.asignatura.AsignaturaResponseDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.repositorios.AsignaturaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsignaturaServicio {
    private final AsignaturaRepositorio asignaturaRepositorio;
    @Transactional
    public AsignaturaResponseDTO crear(AsignaturaRequestDTO request) {
        if (asignaturaRepositorio.existsByCodigoIgnoreCase(request.codigo())) {
            throw new IllegalArgumentException("Ya existe una asignatura con el codigo " + request.codigo());
        }

        Asignatura asignatura = Asignatura.builder()
                .nombre(request.nombre())
                .codigo(request.codigo())
                .baja(false)
                .build();

        asignaturaRepositorio.save(asignatura);
        return AsignaturaResponseDTO.of(asignatura);
    }
    @Transactional(readOnly = true)
    public List<AsignaturaResponseDTO> listarTodas(boolean incluirBajas) {
        return asignaturaRepositorio.findAll().stream()
                .filter(asignatura -> incluirBajas || !asignatura.isBaja())
                .map(AsignaturaResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public AsignaturaResponseDTO obtenerPorId(UUID asignaturaId) {
        return AsignaturaResponseDTO.of(buscarPorIdOLanzar(asignaturaId));
    }
    @Transactional
    public AsignaturaResponseDTO actualizar(UUID asignaturaId, AsignaturaRequestDTO request) {
        Asignatura asignatura = buscarPorIdOLanzar(asignaturaId);

        if (asignaturaRepositorio.existsByCodigoIgnoreCaseAndAsignaturaIdNot(request.codigo(), asignaturaId)) {
            throw new IllegalArgumentException("Ya existe una asignatura con el codigo " + request.codigo());
        }

        asignatura.setNombre(request.nombre());
        asignatura.setCodigo(request.codigo());

        return AsignaturaResponseDTO.of(asignatura);
    }
    @Transactional
    public void darDeBaja(UUID asignaturaId) {
        buscarPorIdOLanzar(asignaturaId).setBaja(true);
    }

    @Transactional
    public void reactivar(UUID asignaturaId) {
        buscarPorIdOLanzar(asignaturaId).setBaja(false);
    }

    private Asignatura buscarPorIdOLanzar(UUID asignaturaId) {
        return asignaturaRepositorio.findById(asignaturaId)
                .orElseThrow(() -> new AsignaturaNoEncontradaException(
                        "No se encontro la asignatura con id " + asignaturaId));
    }
}
