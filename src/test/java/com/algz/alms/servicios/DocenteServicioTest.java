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

import com.algz.alms.dtos.docente.DocenteRegistroRequest;
import com.algz.alms.dtos.docente.DocenteResponse;
import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.Persona;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.excepciones.InvitacionInvalidaException;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.UsuarioDuplicadoException;
import com.algz.alms.repositorios.DocenteRepositorio;
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
@DisplayName("DocenteServicio")
class DocenteServicioTest {

    @Mock
    private DocenteRepositorio docenteRepositorio;
    @Mock
    private PersonaRepositorio personaRepositorio;
    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private InvitacionServicio invitacionServicio;

    @InjectMocks
    private DocenteServicio docenteServicio;

    private DocenteRegistroRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new DocenteRegistroRequest(
            "token-valido", "37500488", "Alvarez Gonzalez", "Hector Jose", "hector@alms.com",
            "381331910", "Necochea 150", "ClaveSegura123"
        );
    }

    @Test
    @DisplayName("registrar valida el token, crea Persona + Usuario + Docente cuando no hay duplicados")
    void registrar_creaTodoCuandoNoHayDuplicados() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_DOCENTE);
        when(usuarioRepositorio.existsByEmail("hector@alms.com")).thenReturn(false);
        when(personaRepositorio.existsByDocumento("37500488")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash-encriptado");
        when(docenteRepositorio.count()).thenReturn(0L);

        DocenteResponse response = docenteServicio.registrar(requestValido);

        verify(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_DOCENTE);

        ArgumentCaptor<Persona> personaCaptor = ArgumentCaptor.forClass(Persona.class);
        verify(personaRepositorio).save(personaCaptor.capture());
        assertThat(personaCaptor.getValue().getDocumento()).isEqualTo("37500488");

        ArgumentCaptor<Docente> docenteCaptor = ArgumentCaptor.forClass(Docente.class);
        verify(docenteRepositorio).save(docenteCaptor.capture());
        assertThat(docenteCaptor.getValue().getLegajo()).startsWith("DO-");
        assertThat(docenteCaptor.getValue().getFechaAlta()).isEqualTo(LocalDate.now());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositorio).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getRol()).isEqualTo(Rol.ROLE_DOCENTE);
        assertThat(usuarioCaptor.getValue().getPassword()).isEqualTo("hash-encriptado");

        assertThat(response.legajo()).startsWith("DO-");
    }

    @Test
    @DisplayName("registrar rechaza si el token de invitación es inválido, sin crear nada")
    void registrar_rechazaTokenInvalido() {
        doThrow(new InvitacionInvalidaException("El enlace de invitación no es válido"))
            .when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_DOCENTE);

        assertThatThrownBy(() -> docenteServicio.registrar(requestValido))
            .isInstanceOf(InvitacionInvalidaException.class);

        verify(personaRepositorio, never()).save(any());
        verify(usuarioRepositorio, never()).save(any());
        verify(docenteRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("registrar rechaza si el email ya está en uso")
    void registrar_rechazaEmailDuplicado() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_DOCENTE);
        when(usuarioRepositorio.existsByEmail("hector@alms.com")).thenReturn(true);

        assertThatThrownBy(() -> docenteServicio.registrar(requestValido))
            .isInstanceOf(UsuarioDuplicadoException.class);

        verify(personaRepositorio, never()).save(any());
        verify(docenteRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("registrar rechaza si el documento ya está registrado")
    void registrar_rechazaDocumentoDuplicado() {
        doNothing().when(invitacionServicio).validarYConsumir("token-valido", Rol.ROLE_DOCENTE);
        when(usuarioRepositorio.existsByEmail("hector@alms.com")).thenReturn(false);
        when(personaRepositorio.existsByDocumento("37500488")).thenReturn(true);

        assertThatThrownBy(() -> docenteServicio.registrar(requestValido))
            .isInstanceOf(PersonaDuplicadaException.class);

        verify(personaRepositorio, never()).save(any());
        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("listar retorna solo docentes activos mapeados a DocenteResponse")
    void listar_retornaSoloActivos() {
        Docente docente = docenteDeEjemplo();
        when(docenteRepositorio.findActivos()).thenReturn(List.of(docente));

        List<DocenteResponse> resultado = docenteServicio.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).legajo()).isEqualTo("DO-2026-0001");
    }

    @Test
    @DisplayName("obtenerPorId retorna el docente cuando existe y está activo")
    void obtenerPorId_retornaCuandoExiste() {
        Docente docente = docenteDeEjemplo();
        when(docenteRepositorio.findActivoPorPersonaId(docente.getPersonaId())).thenReturn(Optional.of(docente));

        DocenteResponse resultado = docenteServicio.obtenerPorId(docente.getPersonaId());

        assertThat(resultado.legajo()).isEqualTo("DO-2026-0001");
    }

    @Test
    @DisplayName("obtenerPorId lanza DocenteNoEncontradoException cuando no existe o no está activo")
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(docenteRepositorio.findActivoPorPersonaId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteServicio.obtenerPorId(id))
            .isInstanceOf(DocenteNoEncontradoException.class);
    }

    private Docente docenteDeEjemplo() {
        Persona persona = Persona.builder()
            .personaId(UUID.randomUUID())
            .documento("37500488")
            .apellidos("Alvarez Gonzalez")
            .nombres("Hector Jose")
            .email("hector@alms.com")
            .telefono("381331910")
            .domicilio("Necochea 150")
            .baja(false)
            .build();
        return Docente.builder()
            .personaId(persona.getPersonaId())
            .persona(persona)
            .legajo("DO-2026-0001")
            .fechaAlta(LocalDate.now())
            .build();
    }
}