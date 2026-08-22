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

import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaRequestDTO;
import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaResponseDTO;
import com.algz.alms.servicios.DocenteAsignaturaServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/docente-asignaturas")
@RequiredArgsConstructor
public class DocenteAsignaturaControlador {
    private final DocenteAsignaturaServicio docenteAsignaturaServicio;

    @PostMapping
    public ResponseEntity<DocenteAsignaturaResponseDTO> crear(
            @Valid @RequestBody DocenteAsignaturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteAsignaturaServicio.crear(request));
    }

    @GetMapping("/asignatura/{asignaturaId}")
    public ResponseEntity<List<DocenteAsignaturaResponseDTO>> listarPorAsignatura(
            @PathVariable UUID asignaturaId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(docenteAsignaturaServicio.listarPorAsignatura(asignaturaId, incluirBajas));
    }
    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<DocenteAsignaturaResponseDTO>> listarPorDocente(
            @PathVariable UUID docenteId,
            @RequestParam(defaultValue = "false") boolean incluirBajas) {
        return ResponseEntity.ok(docenteAsignaturaServicio.listarPorDocente(docenteId, incluirBajas));
    }

    @GetMapping("/{docenteAsignaturaId}")
    public ResponseEntity<DocenteAsignaturaResponseDTO> obtenerPorId(@PathVariable UUID docenteAsignaturaId) {
        return ResponseEntity.ok(docenteAsignaturaServicio.obtenerPorId(docenteAsignaturaId));
    }
    @PatchMapping("/{docenteAsignaturaId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID docenteAsignaturaId) {
        docenteAsignaturaServicio.darDeBaja(docenteAsignaturaId);
        return ResponseEntity.noContent().build();
    }
}
