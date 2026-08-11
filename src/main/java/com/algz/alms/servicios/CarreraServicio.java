package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.carrera.CarreraRequestDTO;
import com.algz.alms.dtos.carrera.CarreraResponseDTO;
import com.algz.alms.entidades.Carrera;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.repositorios.CarreraRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarreraServicio {
    private final CarreraRepositorio carreraRepositorio;

    @Transactional
    public CarreraResponseDTO crear(CarreraRequestDTO request) {
        if (carreraRepositorio.existsByNombreIgnoreCase(request.nombre())) {
            throw new IllegalArgumentException("Ya existe una carrera con ese nombre");
        }

        Carrera carrera = Carrera.builder()
                .nombre(request.nombre())
                .baja(false)
                .build();

        carreraRepositorio.save(carrera);
        return CarreraResponseDTO.of(carrera);
    }
    @Transactional(readOnly = true)
    public List<CarreraResponseDTO> listarTodas(boolean incluirBajas) {
        return carreraRepositorio.findAll().stream()
                .filter(carrera -> incluirBajas || !carrera.isBaja())
                .map(CarreraResponseDTO::of)
                .toList();
    }
    @Transactional(readOnly = true)
    public CarreraResponseDTO obtenerPorId(UUID carreraId) {
        return CarreraResponseDTO.of(buscarPorIdOLanzar(carreraId));
    }

    @Transactional
    public CarreraResponseDTO actualizar(UUID carreraId, CarreraRequestDTO request) {
        Carrera carrera = buscarPorIdOLanzar(carreraId);
        carrera.setNombre(request.nombre());
        return CarreraResponseDTO.of(carrera);
    }

    @Transactional
    public void darDeBaja(UUID carreraId) {
        Carrera carrera = buscarPorIdOLanzar(carreraId);
        carrera.setBaja(true);
    }
    @Transactional
    public void reactivar(UUID carreraId) {
        Carrera carrera = buscarPorIdOLanzar(carreraId);
        carrera.setBaja(false);
    }

    private Carrera buscarPorIdOLanzar(UUID carreraId) {
        return carreraRepositorio.findById(carreraId)
                .orElseThrow(() -> new CarreraNoEncontradaException(
                        "No se encontró la carrera con id " + carreraId));
    }
}
