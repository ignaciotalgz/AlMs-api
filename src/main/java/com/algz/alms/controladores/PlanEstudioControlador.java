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

import com.algz.alms.dtos.planestudio.PlanEstudioRequestDTO;
import com.algz.alms.dtos.planestudio.PlanEstudioResponseDTO;
import com.algz.alms.servicios.PlanEstudioServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/planes-estudio")
@RequiredArgsConstructor
public class PlanEstudioControlador {
    private final PlanEstudioServicio planEstudioServicio;

    @PostMapping
    public ResponseEntity<PlanEstudioResponseDTO> crear(
            @Valid @RequestBody PlanEstudioRequestDTO request) {
        PlanEstudioResponseDTO creado = planEstudioServicio.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<PlanEstudioResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(planEstudioServicio.listarTodos(incluirBajas));
    }

    @GetMapping("/carrera/{carreraId}")
    public ResponseEntity<List<PlanEstudioResponseDTO>> listarPorCarrera(
            @PathVariable UUID carreraId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(planEstudioServicio.listarPorCarrera(carreraId, incluirBajas));
    }

    @GetMapping("/{planEstudioId}")
    public ResponseEntity<PlanEstudioResponseDTO> obtenerPorId(
            @PathVariable UUID planEstudioId) {
        return ResponseEntity.ok(planEstudioServicio.obtenerPorId(planEstudioId));
    }

    @PutMapping("/{planEstudioId}")
    public ResponseEntity<PlanEstudioResponseDTO> actualizar(
            @PathVariable UUID planEstudioId,
            @Valid @RequestBody PlanEstudioRequestDTO request) {
        return ResponseEntity.ok(planEstudioServicio.actualizar(planEstudioId, request));
    }

    @PatchMapping("/{planEstudioId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID planEstudioId) {
        planEstudioServicio.darDeBaja(planEstudioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{planEstudioId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID planEstudioId) {
        planEstudioServicio.reactivar(planEstudioId);
        return ResponseEntity.noContent().build();
    }
}
