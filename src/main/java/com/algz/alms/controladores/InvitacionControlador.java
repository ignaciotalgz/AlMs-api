package com.algz.alms.controladores;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.invitacion.InvitacionRequest;
import com.algz.alms.dtos.invitacion.InvitacionResponse;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.servicios.InvitacionServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/invitaciones")
@RequiredArgsConstructor
public class InvitacionControlador {
    private final InvitacionServicio invitacionServicio;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<InvitacionResponse> generar(@Valid @RequestBody InvitacionRequest request, @AuthenticationPrincipal Usuario usuarioActual) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitacionServicio.generar(request.tipo(), usuarioActual));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{invitacionId}/revocar")
    public ResponseEntity<Void> revocar(@PathVariable UUID invitacionId) {
        invitacionServicio.revocar(invitacionId);
        return ResponseEntity.noContent().build();
    }
}
