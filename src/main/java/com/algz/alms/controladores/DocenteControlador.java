package com.algz.alms.controladores;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.docente.DocenteResponse;
import com.algz.alms.servicios.DocenteServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/docentes")
@RequiredArgsConstructor
public class DocenteControlador {
    private final DocenteServicio docenteServicio;

    @GetMapping
    public ResponseEntity<List<DocenteResponse>> listar() {
        return ResponseEntity.ok(docenteServicio.listar());
    }

    @GetMapping("/{personaId}")
    public ResponseEntity<DocenteResponse> obtener(@PathVariable UUID personaId) {
        return ResponseEntity.ok(docenteServicio.obtenerPorId(personaId));
    }
}   
