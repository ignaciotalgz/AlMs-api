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

import com.algz.alms.dtos.asignatura.AsignaturaRequestDTO;
import com.algz.alms.dtos.asignatura.AsignaturaResponseDTO;
import com.algz.alms.servicios.AsignaturaServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/asignaturas")
@RequiredArgsConstructor
public class AsignaturaControlador {
    private final AsignaturaServicio asignaturaServicio;

    @PostMapping
    public ResponseEntity<AsignaturaResponseDTO> crear(@Valid @RequestBody AsignaturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asignaturaServicio.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<AsignaturaResponseDTO>> listarTodas(
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(asignaturaServicio.listarTodas(incluirBajas));
    }
    @GetMapping("/{asignaturaId}")
    public ResponseEntity<AsignaturaResponseDTO> obtenerPorId(@PathVariable UUID asignaturaId) {
        return ResponseEntity.ok(asignaturaServicio.obtenerPorId(asignaturaId));
    }

    @PutMapping("/{asignaturaId}")
    public ResponseEntity<AsignaturaResponseDTO> actualizar(
            @PathVariable UUID asignaturaId,
            @Valid @RequestBody AsignaturaRequestDTO request) {
        return ResponseEntity.ok(asignaturaServicio.actualizar(asignaturaId, request));
    }
    @PatchMapping("/{asignaturaId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID asignaturaId) {
        asignaturaServicio.darDeBaja(asignaturaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{asignaturaId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID asignaturaId) {
        asignaturaServicio.reactivar(asignaturaId);
        return ResponseEntity.noContent().build();
    }
}
