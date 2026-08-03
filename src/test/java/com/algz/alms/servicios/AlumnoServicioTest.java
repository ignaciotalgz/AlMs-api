package com.algz.alms.servicios;

import java.time.LocalDate;
import java.util.List;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.algz.alms.dtos.alumno.AlumnoRegistroRequest;
import com.algz.alms.dtos.alumno.AlumnoResponse;
import com.algz.alms.entidades.Alumno;
import com.algz.alms.entidades.Persona;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.excepciones.AlumnoNoEncontradoException;
import com.algz.alms.excepciones.InvitacionInvalidaException;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.UsuarioDuplicadoException;
import com.algz.alms.repositorios.AlumnoRepositorio;
import com.algz.alms.repositorios.PersonaRepositorio;
import com.algz.alms.repositorios.UsuarioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlumnoServicio")
class AlumnoServicioTest {

    @Mock
    private AlumnoRepositorio alumnoRepositorio;
    @Mock
    private PersonaRepositorio personaRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private InvitacionServicio invitacionServicio;

    @InjectMocks
    private AlumnoServicio alumnoServicio;

    private AlumnoRegistroRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new AlumnoRegistroRequest(
            "token-valido", "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "ClaveSegura123"
        );
    }

    @Test
    @DisplayName("registrar valida el token, crea Persona + Usuario + Alumno cuando no hay duplicados")
    void registrar_creaTodoCuandoNoHayDuplicados() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_ALUMNO);
        when(usuarioRepositorio.existsByEmail("ignacio@alms.com")).thenReturn(false);
        when(personaRepositorio.existsByDocumento("39698370")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash-encriptado");
        when(alumnoRepositorio.count()).thenReturn(0L);

        AlumnoResponse response = alumnoServicio.registrar(requestValido);

        verify(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_ALUMNO);

        ArgumentCaptor<Persona> personaCaptor = ArgumentCaptor.forClass(Persona.class);
        verify(personaRepositorio).save(personaCaptor.capture());
        assertThat(personaCaptor.getValue().getDocumento()).isEqualTo("39698370");

        ArgumentCaptor<Alumno> alumnoCaptor = ArgumentCaptor.forClass(Alumno.class);
        verify(alumnoRepositorio).save(alumnoCaptor.capture());
        assertThat(alumnoCaptor.getValue().getLegajo()).startsWith("AL-");
        assertThat(alumnoCaptor.getValue().getFechaAlta()).isEqualTo(LocalDate.now());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositorio).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getRol()).isEqualTo(Rol.ROLE_ALUMNO);
        assertThat(usuarioCaptor.getValue().getPassword()).isEqualTo("hash-encriptado");

        assertThat(response.legajo()).startsWith("AL-");
    }

    @Test
    @DisplayName("registrar rechaza si el token de invitación es inválido, sin crear nada")
    void registrar_rechazaTokenInvalido() {
        doThrow(new InvitacionInvalidaException("El enlace de invitación no es válido"))
            .when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_ALUMNO);

        assertThatThrownBy(() -> alumnoServicio.registrar(requestValido))
            .isInstanceOf(InvitacionInvalidaException.class);

        verify(personaRepositorio, never()).save(any());
        verify(usuarioRepositorio, never()).save(any());
        verify(alumnoRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("registrar rechaza si el email ya está en uso")
    void registrar_rechazaEmailDuplicado() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_ALUMNO);
        when(usuarioRepositorio.existsByEmail("ignacio@alms.com")).thenReturn(true);

        assertThatThrownBy(() -> alumnoServicio.registrar(requestValido))
            .isInstanceOf(UsuarioDuplicadoException.class);

        verify(personaRepositorio, never()).save(any());
        verify(alumnoRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("registrar rechaza si el documento ya está registrado")
    void registrar_rechazaDocumentoDuplicado() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_ALUMNO);
        when(usuarioRepositorio.existsByEmail("ignacio@alms.com")).thenReturn(false);
        when(personaRepositorio.existsByDocumento("39698370")).thenReturn(true);

        assertThatThrownBy(() -> alumnoServicio.registrar(requestValido))
            .isInstanceOf(PersonaDuplicadaException.class);

        verify(personaRepositorio, never()).save(any());
        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("listar retorna solo alumnos activos mapeados a AlumnoResponse")
    void listar_retornaSoloActivos() {
        Alumno alumno = alumnoDeEjemplo();
        when(alumnoRepositorio.findActivos()).thenReturn(List.of(alumno));

        List<AlumnoResponse> resultado = alumnoServicio.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).legajo()).isEqualTo("AL-2026-0001");
    }

    @Test
    @DisplayName("obtenerPorId retorna el alumno cuando existe y está activo")
    void obtenerPorId_retornaCuandoExiste() {
        Alumno alumno = alumnoDeEjemplo();
        when(alumnoRepositorio.findActivoPorPersonaId(alumno.getPersonaId())).thenReturn(Optional.of(alumno));

        AlumnoResponse resultado = alumnoServicio.obtenerPorId(alumno.getPersonaId());

        assertThat(resultado.legajo()).isEqualTo("AL-2026-0001");
    }

    @Test
    @DisplayName("obtenerPorId lanza AlumnoNoEncontradoException cuando no existe o no está activo")
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(alumnoRepositorio.findActivoPorPersonaId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alumnoServicio.obtenerPorId(id))
            .isInstanceOf(AlumnoNoEncontradoException.class);
    }

    private Alumno alumnoDeEjemplo() {
        Persona persona = Persona.builder()
            .personaId(UUID.randomUUID())
            .documento("39698370")
            .apellidos("Alvarez Gonzalez")
            .nombres("Ignacio Tomás")
            .email("ignacio@alms.com")
            .telefono("3813527690")
            .domicilio("Don bosco 2579")
            .baja(false)
            .build();
        return Alumno.builder()
            .personaId(persona.getPersonaId())
            .persona(persona)
            .legajo("AL-2026-0001")
            .fechaAlta(LocalDate.now())
            .build();
    }
}