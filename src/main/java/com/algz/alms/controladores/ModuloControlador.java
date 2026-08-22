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

import com.algz.alms.dtos.modulo.ModuloRequestDTO;
import com.algz.alms.dtos.modulo.ModuloResponseDTO;
import com.algz.alms.servicios.ModuloServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/modulos")
@RequiredArgsConstructor
public class ModuloControlador {
    private final ModuloServicio moduloServicio;

    @PostMapping
    public ResponseEntity<ModuloResponseDTO> crear(@Valid @RequestBody ModuloRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduloServicio.crear(request));
    }

    @GetMapping("/plan/{planEstudioId}")
    public ResponseEntity<List<ModuloResponseDTO>> listarPorPlan(
            @PathVariable UUID planEstudioId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(moduloServicio.listarPorPlan(planEstudioId, incluirBajas));
    }
    @GetMapping("/{moduloId}")
    public ResponseEntity<ModuloResponseDTO> obtenerPorId(@PathVariable UUID moduloId) {
        return ResponseEntity.ok(moduloServicio.obtenerPorId(moduloId));
    }

    @PutMapping("/{moduloId}")
    public ResponseEntity<ModuloResponseDTO> actualizar(
            @PathVariable UUID moduloId,
            @Valid @RequestBody ModuloRequestDTO request) {
        return ResponseEntity.ok(moduloServicio.actualizar(moduloId, request));
    }
    @PatchMapping("/{moduloId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID moduloId) {
        moduloServicio.darDeBaja(moduloId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{moduloId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID moduloId) {
        moduloServicio.reactivar(moduloId);
        return ResponseEntity.noContent().build();
    }
}
