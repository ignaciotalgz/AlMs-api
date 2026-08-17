package com.algz.alms.controladores;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.carrera.CarreraRequestDTO;
import com.algz.alms.dtos.carrera.CarreraResponseDTO;
import com.algz.alms.servicios.CarreraServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/carreras")
@RequiredArgsConstructor
public class CarreraControlador {
    private final CarreraServicio carreraServicio;

    @PostMapping
    public ResponseEntity<CarreraResponseDTO> crear(@Valid @RequestBody CarreraRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraServicio.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<CarreraResponseDTO>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(carreraServicio.listarTodas(incluirBajas));
    }

    @GetMapping("/{carreraId}")
    public ResponseEntity<CarreraResponseDTO> obtenerPorId(@PathVariable UUID carreraId) {
        return ResponseEntity.ok(carreraServicio.obtenerPorId(carreraId));
    }

    @PutMapping("/{carreraId}")
    public ResponseEntity<CarreraResponseDTO> actualizar(
            @PathVariable UUID carreraId,
            @Valid @RequestBody CarreraRequestDTO request) {
        return ResponseEntity.ok(carreraServicio.actualizar(carreraId, request));
    }

    @PatchMapping("/{carreraId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID carreraId) {
        carreraServicio.darDeBaja(carreraId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{carreraId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID carreraId) {
        carreraServicio.reactivar(carreraId);
        return ResponseEntity.noContent().build();
    }
}
