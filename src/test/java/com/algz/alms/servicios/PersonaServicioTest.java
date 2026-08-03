package com.algz.alms.servicios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.persona.PersonaRequest;
import com.algz.alms.dtos.persona.PersonaResponse;
import com.algz.alms.entidades.Persona;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.PersonaNoEncontradaException;
import com.algz.alms.repositorios.PersonaRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonaServicio")
class PersonaServicioTest {

    @Mock
    private PersonaRepositorio personaRepositorio;

    @InjectMocks
    private PersonaServicio personaServicio;

    private PersonaRequest requestValido;

    @BeforeEach
    void setUp() {
        requestValido = new PersonaRequest("39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
    }

    @Test
    @DisplayName("crear guarda la Persona cuando el documento no está registrado")
    void crear_guardaCuandoNoHayDuplicado() {
        when(personaRepositorio.existsByDocumento("39698370")).thenReturn(false);

        PersonaResponse response = personaServicio.crear(requestValido);

        verify(personaRepositorio).save(any(Persona.class));
        assertThat(response.documento()).isEqualTo("39698370");
        assertThat(response.nombres()).isEqualTo("Ignacio Tomás");
    }

    @Test
    @DisplayName("crear rechaza si el documento ya está registrado")
    void crear_rechazaDocumentoDuplicado() {
        when(personaRepositorio.existsByDocumento("39698370")).thenReturn(true);

        assertThatThrownBy(() -> personaServicio.crear(requestValido))
            .isInstanceOf(PersonaDuplicadaException.class);

        verify(personaRepositorio, never()).save(any());
    }

    @Test
    @DisplayName("listar retorna solo personas activas")
    void listar_retornaSoloActivas() {
        Persona persona = personaDeEjemplo();
        when(personaRepositorio.findByBajaFalse()).thenReturn(List.of(persona));

        List<PersonaResponse> resultado = personaServicio.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).documento()).isEqualTo("39698370");
    }

    @Test
    @DisplayName("obtenerPorId retorna la persona cuando existe y está activa")
    void obtenerPorId_retornaCuandoExiste() {
        Persona persona = personaDeEjemplo();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(persona.getPersonaId())).thenReturn(Optional.of(persona));

        PersonaResponse resultado = personaServicio.obtenerPorId(persona.getPersonaId());

        assertThat(resultado.documento()).isEqualTo("39698370");
    }

    @Test
    @DisplayName("obtenerPorId lanza PersonaNoEncontradaException cuando no existe")
    void obtenerPorId_lanzaExcepcionCuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaServicio.obtenerPorId(id))
            .isInstanceOf(PersonaNoEncontradaException.class);
    }

    @Test
    @DisplayName("actualizar modifica los datos cuando el documento no cambia")
    void actualizar_modificaDatosSinCambiarDocumento() {
        Persona persona = personaDeEjemplo();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(persona.getPersonaId())).thenReturn(Optional.of(persona));
        PersonaRequest request = new PersonaRequest("39698370", "Nuevo Apellido", "Nuevo Nombre", "nuevo@alms.com", "3810000000", "Nuevo domicilio");

        PersonaResponse resultado = personaServicio.actualizar(persona.getPersonaId(), request);

        assertThat(resultado.apellidos()).isEqualTo("Nuevo Apellido");
        assertThat(resultado.email()).isEqualTo("nuevo@alms.com");
        verify(personaRepositorio, never()).existsByDocumento(any());
    }

    @Test
    @DisplayName("actualizar rechaza si el nuevo documento ya pertenece a otra persona")
    void actualizar_rechazaSiNuevoDocumentoYaExiste() {
        Persona persona = personaDeEjemplo();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(persona.getPersonaId())).thenReturn(Optional.of(persona));
        when(personaRepositorio.existsByDocumento("11111111")).thenReturn(true);
        PersonaRequest request = new PersonaRequest("11111111", "Apellido", "Nombre", "email@alms.com", "0000000000", "domicilio");

        assertThatThrownBy(() -> personaServicio.actualizar(persona.getPersonaId(), request))
            .isInstanceOf(PersonaDuplicadaException.class);
    }

    @Test
    @DisplayName("darDeBaja marca baja=true y persiste")
    void darDeBaja_marcaBajaYPersiste() {
        Persona persona = personaDeEjemplo();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(persona.getPersonaId())).thenReturn(Optional.of(persona));

        personaServicio.darDeBaja(persona.getPersonaId());

        assertThat(persona.isBaja()).isTrue();
        verify(personaRepositorio).save(persona);
    }

    @Test
    @DisplayName("darDeBaja lanza PersonaNoEncontradaException si no existe activa")
    void darDeBaja_lanzaExcepcionSiNoExisteActiva() {
        UUID id = UUID.randomUUID();
        when(personaRepositorio.findByPersonaIdAndBajaFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaServicio.darDeBaja(id))
            .isInstanceOf(PersonaNoEncontradaException.class);
    }

    private Persona personaDeEjemplo() {
        return Persona.builder()
            .personaId(UUID.randomUUID())
            .documento("39698370")
            .apellidos("Alvarez Gonzalez")
            .nombres("Ignacio Tomás")
            .email("ignacio@alms.com")
            .telefono("3813527690")
            .domicilio("Don bosco 2579")
            .baja(false)
            .build();
    }
}