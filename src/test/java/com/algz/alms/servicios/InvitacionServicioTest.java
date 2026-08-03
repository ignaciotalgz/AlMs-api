package com.algz.alms.servicios;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.algz.alms.dtos.invitacion.InvitacionResponse;
import com.algz.alms.entidades.InvitacionRegistro;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.enumeraciones.TipoInvitacion;
import com.algz.alms.excepciones.InvitacionInvalidaException;
import com.algz.alms.excepciones.InvitacionNoEncontradaException;
import com.algz.alms.repositorios.InvitacionRegistroRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvitacionServicio")
public class InvitacionServicioTest {
    @Mock
    private InvitacionRegistroRepositorio invitacionRegistroRepositorio;
    @Mock
    private TokenServicio tokenServicio;

    @InjectMocks
    private InvitacionServicio invitacionServicio;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitacionServicio, "horasValidez", 24L);
        admin = Usuario.builder().rol(Rol.ROLE_ADMIN).email("admin@alms.com").build();
    }
    @Test
    @DisplayName("generar crea la invitación con el hash del token y devuelve el token crudo")
    void generar_creaInvitacionYDevuelveTokenCrudo() {
        when(tokenServicio.generarToken()).thenReturn("token-crudo-123");
        when(tokenServicio.hash("token-crudo-123")).thenReturn("hash-abc");

        InvitacionResponse response = invitacionServicio.generar(TipoInvitacion.ALUMNO, admin);

        ArgumentCaptor<InvitacionRegistro> captor = ArgumentCaptor.forClass(InvitacionRegistro.class);
        verify(invitacionRegistroRepositorio).save(captor.capture());

        assertThat(captor.getValue().getTokenHash()).isEqualTo("hash-abc");
        assertThat(captor.getValue().getRol()).isEqualTo(Rol.ROLE_ALUMNO);
        assertThat(captor.getValue().isUsada()).isFalse();
        assertThat(captor.getValue().getCreadaPor()).isEqualTo(admin);

        assertThat(response.token()).isEqualTo("token-crudo-123");
        assertThat(response.tipo()).isEqualTo(TipoInvitacion.ALUMNO);
    }

    @Test
    @DisplayName("validarYConsumir marca la invitación como usada cuando es válida")
    void validarYConsumir_marcaUsadaCuandoEsValida() {
        when(tokenServicio.hash("token-valido")).thenReturn("hash-valido");
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        when(invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-valido")).thenReturn(Optional.of(invitacion));

        invitacionServicio.validarYConsumir("token-valido", Rol.ROLE_ALUMNO);

        assertThat(invitacion.isUsada()).isTrue();
        assertThat(invitacion.getFechaUso()).isNotNull();
        verify(invitacionRegistroRepositorio).save(invitacion);
    }

    @Test
    @DisplayName("validarYConsumir lanza InvitacionInvalidaException si el token no existe")
    void validarYConsumir_lanzaExcepcionSiNoExiste() {
        when(tokenServicio.hash("token-inexistente")).thenReturn("hash-inexistente");
        when(invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitacionServicio.validarYConsumir("token-inexistente", Rol.ROLE_ALUMNO))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    @Test
    @DisplayName("validarYConsumir lanza InvitacionInvalidaException si ya fue usada")
    void validarYConsumir_lanzaExcepcionSiYaUsada() {
        when(tokenServicio.hash("token-usado")).thenReturn("hash-usado");
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        invitacion.setUsada(true);
        when(invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-usado")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionServicio.validarYConsumir("token-usado", Rol.ROLE_ALUMNO))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    @Test
    @DisplayName("validarYConsumir lanza InvitacionInvalidaException si expiró")
    void validarYConsumir_lanzaExcepcionSiExpiro() {
        when(tokenServicio.hash("token-expirado")).thenReturn("hash-expirado");
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        invitacion.setFechaExpiracion(LocalDateTime.now().minusHours(1));
        when(invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-expirado")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionServicio.validarYConsumir("token-expirado", Rol.ROLE_ALUMNO))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    @Test
    @DisplayName("validarYConsumir lanza InvitacionInvalidaException si el rol no coincide")
    void validarYConsumir_lanzaExcepcionSiRolNoCoincide() {
        when(tokenServicio.hash("token-docente")).thenReturn("hash-docente");
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_DOCENTE);
        when(invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-docente")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionServicio.validarYConsumir("token-docente", Rol.ROLE_ALUMNO))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    @Test
    @DisplayName("revocar marca la invitación como usada cuando está vigente")
    void revocar_marcaUsadaCuandoVigente() {
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        UUID id = UUID.randomUUID();
        when(invitacionRegistroRepositorio.findByIdParaActualizar(id)).thenReturn(Optional.of(invitacion));

        invitacionServicio.revocar(id);

        assertThat(invitacion.isUsada()).isTrue();
        assertThat(invitacion.getFechaUso()).isNotNull();
        verify(invitacionRegistroRepositorio).save(invitacion);
    }

    @Test
    @DisplayName("revocar lanza InvitacionNoEncontradaException si el id no existe")
    void revocar_lanzaExcepcionSiNoExiste() {
        UUID id = UUID.randomUUID();
        when(invitacionRegistroRepositorio.findByIdParaActualizar(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invitacionServicio.revocar(id))
            .isInstanceOf(InvitacionNoEncontradaException.class);
    }

    @Test
    @DisplayName("revocar lanza InvitacionInvalidaException si ya estaba usada/revocada")
    void revocar_lanzaExcepcionSiYaUsada() {
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        invitacion.setUsada(true);
        UUID id = UUID.randomUUID();
        when(invitacionRegistroRepositorio.findByIdParaActualizar(id)).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionServicio.revocar(id))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    @Test
    @DisplayName("revocar lanza InvitacionInvalidaException si ya expiró")
    void revocar_lanzaExcepcionSiYaExpiro() {
        InvitacionRegistro invitacion = invitacionActiva(Rol.ROLE_ALUMNO);
        invitacion.setFechaExpiracion(LocalDateTime.now().minusMinutes(1));
        UUID id = UUID.randomUUID();
        when(invitacionRegistroRepositorio.findByIdParaActualizar(id)).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> invitacionServicio.revocar(id))
            .isInstanceOf(InvitacionInvalidaException.class);
    }

    private InvitacionRegistro invitacionActiva(Rol rol) {
        return InvitacionRegistro.builder()
            .invitacionId(UUID.randomUUID())
            .tokenHash("hash-cualquiera")
            .rol(rol)
            .usada(false)
            .fechaCreacion(LocalDateTime.now())
            .fechaExpiracion(LocalDateTime.now().plusHours(24))
            .build();
    }
}
