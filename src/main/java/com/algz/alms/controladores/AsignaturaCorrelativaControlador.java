package com.algz.alms.controladores;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaRequestDTO;
import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaResponseDTO;
import com.algz.alms.servicios.AsignaturaCorrelativaServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/asignatura-correlativas")
@RequiredArgsConstructor
public class AsignaturaCorrelativaControlador {
    private final AsignaturaCorrelativaServicio asignaturaCorrelativaServicio;
    @PostMapping
    public ResponseEntity<AsignaturaCorrelativaResponseDTO> crear(
            @Valid @RequestBody AsignaturaCorrelativaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asignaturaCorrelativaServicio.crear(request));
    }

    @GetMapping("/modulo-asignatura/{moduloAsignaturaId}")
    public ResponseEntity<List<AsignaturaCorrelativaResponseDTO>> listarPorModuloAsignatura(
            @PathVariable UUID moduloAsignaturaId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(
                asignaturaCorrelativaServicio.listarPorModuloAsignatura(moduloAsignaturaId, incluirBajas));
    }
    @GetMapping("/{asignaturaCorrelativaId}")
    public ResponseEntity<AsignaturaCorrelativaResponseDTO> obtenerPorId(
            @PathVariable UUID asignaturaCorrelativaId) {
        return ResponseEntity.ok(asignaturaCorrelativaServicio.obtenerPorId(asignaturaCorrelativaId));
    }

    @PatchMapping("/{asignaturaCorrelativaId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID asignaturaCorrelativaId) {
        asignaturaCorrelativaServicio.darDeBaja(asignaturaCorrelativaId);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{asignaturaCorrelativaId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID asignaturaCorrelativaId) {
        asignaturaCorrelativaServicio.reactivar(asignaturaCorrelativaId);
        return ResponseEntity.noContent().build();
    }
}
