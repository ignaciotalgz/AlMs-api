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

import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.Persona;

@DataJpaTest
@DisplayName("DocenteRepositorio")
public class DocenteRepositorioTest {
    @Autowired
    private DocenteRepositorio docenteRepositorio;
    @Autowired
    private PersonaRepositorio personaRepositorio;

    private Docente activo;
    private Docente personaDadaDeBaja;


    @BeforeEach
    void setUp() {
        docenteRepositorio.deleteAll();
        personaRepositorio.deleteAll();

        Persona personaActiva = personaRepositorio.save(Persona.builder()
            .documento("37500488")
            .apellidos("Alvarez Gonzalez")
            .nombres("Hector Jose")
            .email("aghj@alms.com")
            .telefono("381331910")
            .domicilio("Necochea 150")
            .baja(false)
            .build());
        activo = docenteRepositorio.save(Docente.builder()
            .persona(personaActiva)
            .legajo("DO-2026-0001")
            .fechaAlta(LocalDate.now())
            .build());
            Persona personaBaja = personaRepositorio.save(Persona.builder()
            .documento("00000001")
            .apellidos("Apellidos")
            .nombres("Nombres")
            .email("null2@alms.com")
            .telefono("0000000000")
            .domicilio("null 000")
            .baja(true)
            .build());
        personaDadaDeBaja = docenteRepositorio.save(Docente.builder()
            .persona(personaBaja)
            .legajo("DO-2026-0002")
            .fechaAlta(LocalDate.now())
            .build());
    }

    @Test
    @DisplayName("findActivos retorna solo los docentes cuya Persona está activa")
    void findActivos_retornaSoloActivos() {
        List<Docente> activos = docenteRepositorio.findActivos();
        assertThat(activos).hasSize(1);
        assertThat(activos).extracting(Docente::getLegajo).containsExactly("DO-2026-0001");
    }

    @Test
    @DisplayName("findActivoPorPersonaId retorna el docente si su Persona está activa")
    void findActivoPorPersonaId_retornaActivo() {
        Optional<Docente> resultado = docenteRepositorio.findActivoPorPersonaId(activo.getPersonaId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getLegajo()).isEqualTo("DO-2026-0001");
    }
    @Test
    @DisplayName("findActivoPorPersonaId retorna vacío si la Persona está dada de baja")
    void findActivoPorPersonaId_retornaVacioSiPersonaDadaDeBaja() {
        Optional<Docente> resultado = docenteRepositorio.findActivoPorPersonaId(personaDadaDeBaja.getPersonaId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findActivoPorPersonaId retorna vacío si el UUID no existe")
    void findActivoPorPersonaId_retornaVacioSiNoExiste() {
        Optional<Docente> resultado = docenteRepositorio.findActivoPorPersonaId(UUID.randomUUID());
        assertThat(resultado).isEmpty();
    }
    @Test
    @DisplayName("el personaId de Docente coincide con el personaId de su Persona (PK compartida)")
    void personaId_esElMismoQueElDeLaPersonaAsociada() {
        assertThat(activo.getPersonaId()).isEqualTo(activo.getPersona().getPersonaId());
    }
}
