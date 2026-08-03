package com.algz.alms.servicios;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.invitacion.InvitacionResponse;
import com.algz.alms.entidades.InvitacionRegistro;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.enumeraciones.TipoInvitacion;
import com.algz.alms.excepciones.InvitacionInvalidaException;
import com.algz.alms.excepciones.InvitacionNoEncontradaException;
import com.algz.alms.repositorios.InvitacionRegistroRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvitacionServicio {
    private final InvitacionRegistroRepositorio invitacionRegistroRepositorio;
    private final TokenServicio tokenServicio;

    @Value("${invitacion.horas-validez:24}")
    private long horasValidez;

    @Transactional
    public InvitacionResponse generar(TipoInvitacion tipo, Usuario creadoPor) {
        String tokenCrudo = tokenServicio.generarToken();
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusHours(horasValidez);

        InvitacionRegistro invitacion = InvitacionRegistro.builder()
            .tokenHash(tokenServicio.hash(tokenCrudo))
            .rol(tipo.getRolAsociado())
            .usada(false)
            .fechaCreacion(ahora)
            .fechaExpiracion(expiracion)
            .creadaPor(creadoPor)
            .build();
        invitacionRegistroRepositorio.save(invitacion);

        return new InvitacionResponse(tokenCrudo, tipo, expiracion);
    }

    @Transactional
    public void validarYConsumir(String tokenCrudo, Rol rolEsperado) {
        String tokenHash = tokenServicio.hash(tokenCrudo);

        InvitacionRegistro invitacion = invitacionRegistroRepositorio.findByTokenHashParaActualizar(tokenHash)
            .orElseThrow(() -> new InvitacionInvalidaException("El enlace de invitación no es válido"));

        if (invitacion.isUsada()) {
            throw new InvitacionInvalidaException("El enlace de invitación ya fue utilizado");
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new InvitacionInvalidaException("El enlace de invitación expiró");
        }
        if (invitacion.getRol() != rolEsperado) {
            throw new InvitacionInvalidaException("El enlace de invitación no corresponde a este tipo de registro");
        }

        invitacion.setUsada(true);
        invitacion.setFechaUso(LocalDateTime.now());
        invitacionRegistroRepositorio.save(invitacion);
    }

    @Transactional
    public void revocar(UUID invitacionId) {
        InvitacionRegistro invitacion = invitacionRegistroRepositorio.findByIdParaActualizar(invitacionId).orElseThrow(() -> new InvitacionNoEncontradaException("Invitacion no encontrada: " + invitacionId));
        if (invitacion.isUsada()) {
            throw new InvitacionInvalidaException("La invitacion ya fue utilizada o revocada");
        }
        if (invitacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new InvitacionInvalidaException("La invitacion ya expiro");
        }
        invitacion.setUsada(true);
        invitacion.setFechaUso(LocalDateTime.now());
        invitacionRegistroRepositorio.save(invitacion);
    }
}
