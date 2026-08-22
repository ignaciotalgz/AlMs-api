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

import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaRequestDTO;
import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaResponseDTO;
import com.algz.alms.servicios.ModuloAsignaturaServicio;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/modulo-asignaturas")
@RequiredArgsConstructor
public class ModuloAsignaturaControlador {
    private final ModuloAsignaturaServicio moduloAsignaturaServicio;
    @PostMapping
    public ResponseEntity<ModuloAsignaturaResponseDTO> crear(
            @Valid @RequestBody ModuloAsignaturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduloAsignaturaServicio.crear(request));
    }

    @GetMapping("/modulo/{moduloId}")
    public ResponseEntity<List<ModuloAsignaturaResponseDTO>> listarPorModulo(
            @PathVariable UUID moduloId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(moduloAsignaturaServicio.listarPorModulo(moduloId, incluirBajas));
    }
    @GetMapping("/{moduloAsignaturaId}")
    public ResponseEntity<ModuloAsignaturaResponseDTO> obtenerPorId(@PathVariable UUID moduloAsignaturaId) {
        return ResponseEntity.ok(moduloAsignaturaServicio.obtenerPorId(moduloAsignaturaId));
    }

    @PatchMapping("/{moduloAsignaturaId}/orden")
    public ResponseEntity<ModuloAsignaturaResponseDTO> actualizarOrden(
            @PathVariable UUID moduloAsignaturaId,
            @Valid @RequestBody ActualizarOrdenRequest request) {
        return ResponseEntity.ok(moduloAsignaturaServicio.actualizarOrden(moduloAsignaturaId, request.orden()));
    }
    @PatchMapping("/{moduloAsignaturaId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID moduloAsignaturaId) {
        moduloAsignaturaServicio.darDeBaja(moduloAsignaturaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{moduloAsignaturaId}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID moduloAsignaturaId) {
        moduloAsignaturaServicio.reactivar(moduloAsignaturaId);
        return ResponseEntity.noContent().build();
    }
    public record ActualizarOrdenRequest(
            @NotNull(message = "El orden es obligatorio")
            @Positive(message = "El orden debe ser un numero positivo")
            Integer orden
    ) {}
}
