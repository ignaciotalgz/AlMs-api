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
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.persona.PersonaRequest;
import com.algz.alms.dtos.persona.PersonaResponse;
import com.algz.alms.servicios.PersonaServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaControlador {
    private final PersonaServicio personaServicio;

    @GetMapping
    public ResponseEntity<List<PersonaResponse>> listar() {
        return ResponseEntity.ok(personaServicio.listar());
    }
    @PostMapping
    public ResponseEntity<PersonaResponse> crear(@Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personaServicio.crear(request));
    }
    @PutMapping("/{personaId}")
    public ResponseEntity<PersonaResponse> actualizar(@PathVariable UUID personaId, @Valid @RequestBody PersonaRequest request) {
        return ResponseEntity.ok(personaServicio.actualizar(personaId, request));
    }
    @PatchMapping("/{personaId}/baja")
    public ResponseEntity<Void> darDeBaja(@PathVariable UUID personaId) {
        personaServicio.darDeBaja(personaId);
        return ResponseEntity.noContent().build();
    }
}
