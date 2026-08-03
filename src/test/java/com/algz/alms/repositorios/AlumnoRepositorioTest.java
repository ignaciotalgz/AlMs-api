package com.algz.alms.repositorios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.algz.alms.entidades.Alumno;
import com.algz.alms.entidades.Persona;

@DataJpaTest
@DisplayName("AlumnoRepositorio")
public class AlumnoRepositorioTest {
    @Autowired
    private AlumnoRepositorio alumnoRepositorio;
    @Autowired
    private PersonaRepositorio personaRepositorio;

    private Alumno activo;
    private Alumno personaDadaDeBaja;

    @BeforeEach
    void setUp() {
        alumnoRepositorio.deleteAll();
        personaRepositorio.deleteAll();

        Persona personaActiva = personaRepositorio.save(Persona.builder()
            .documento("39698370")
            .apellidos("Alvarez Gonzalez")
            .nombres("Ignacio Tomás")
            .email("agit@alms.com")
            .telefono("3813527690")
            .domicilio("Don bosco 2579")
            .baja(false)
            .build());
        activo = alumnoRepositorio.save(Alumno.builder()
            .persona(personaActiva)
            .legajo("AL-2026-0001")
            .fechaAlta(LocalDate.now())
            .build());
            Persona personaBaja = personaRepositorio.save(Persona.builder()
            .documento("00000000")
            .apellidos("Apellidos")
            .nombres("Nombres")
            .email("null@alms.com")
            .telefono("0000000000")
            .domicilio("null 000")
            .baja(true)
            .build());
        personaDadaDeBaja = alumnoRepositorio.save(Alumno.builder()
            .persona(personaBaja)
            .legajo("AL-2026-0002")
            .fechaAlta(LocalDate.now())
            .build());
    }

    @Test
    @DisplayName("findActivos retorna solo los alumnos cuya Persona está activa")
    void findActivos_retornaSoloActivos() {
        List<Alumno> activos = alumnoRepositorio.findActivos();
        assertThat(activos).hasSize(1);
        assertThat(activos).extracting(Alumno::getLegajo).containsExactly("AL-2026-0001");
    }

    @Test
    @DisplayName("findActivoPorPersonaId retorna el alumno si su Persona está activa")
    void findActivoPorPersonaId_retornaActivo() {
        Optional<Alumno> resultado = alumnoRepositorio.findActivoPorPersonaId(activo.getPersonaId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getLegajo()).isEqualTo("AL-2026-0001");
    }
    @Test
    @DisplayName("findActivoPorPersonaId retorna vacío si la Persona está dada de baja")
    void findActivoPorPersonaId_retornaVacioSiPersonaDadaDeBaja() {
        Optional<Alumno> resultado = alumnoRepositorio.findActivoPorPersonaId(personaDadaDeBaja.getPersonaId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findActivoPorPersonaId retorna vacío si el UUID no existe")
    void findActivoPorPersonaId_retornaVacioSiNoExiste() {
        Optional<Alumno> resultado = alumnoRepositorio.findActivoPorPersonaId(UUID.randomUUID());
        assertThat(resultado).isEmpty();
    }
    @Test
    @DisplayName("el personaId de Alumno coincide con el personaId de su Persona (PK compartida)")
    void personaId_esElMismoQueElDeLaPersonaAsociada() {
        assertThat(activo.getPersonaId()).isEqualTo(activo.getPersona().getPersonaId());
    }
}
