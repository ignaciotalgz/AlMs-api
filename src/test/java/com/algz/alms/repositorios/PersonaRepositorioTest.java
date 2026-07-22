package com.algz.alms.repositorios;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.algz.alms.entidades.Persona;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PersonaRepositorio")
public class PersonaRepositorioTest {
    @Autowired
    private PersonaRepositorio personaRepositorio;

    private Persona ignacio;
    private Persona jose;
    private Persona dadaDeBaja;

    @BeforeEach
    void setUp() {
        personaRepositorio.deleteAll();
        ignacio = personaRepositorio.save(Persona.builder()
            .documento("39698370")
            .apellidos("Alvarez Gonzalez")
            .nombres("Ignacio Tomás")
            .email("agit@alms.com")
            .telefono("3813527690")
            .domicilio("Don bosco 2579")
            .baja(false)
            .build()
        );   
        jose = personaRepositorio.save(Persona.builder()
            .documento("37500488")
            .apellidos("Alvarez Gonzalez")
            .nombres("Hector Jose")
            .email("aghj@alms.com")
            .telefono("381331910")
            .domicilio("Necochea 150")
            .baja(false)
            .build()
        );
        dadaDeBaja = personaRepositorio.save(Persona.builder()
            .documento("00000000")
            .apellidos("Apellidos")
            .nombres("Nombres")
            .email("null@alms.com")
            .telefono("0000000000")
            .domicilio("null 000")
            .baja(true)
            .build()
        );
    }

    @Test
    @DisplayName("findByBajaFalse retorna solo las personas activas")
    void findByBajaFalse_retornaSoloActivas() {
        List<Persona> activas = personaRepositorio.findByBajaFalse();
        assertThat(activas).hasSize(2);
        assertThat(activas).extracting(Persona::getDocumento).containsExactlyInAnyOrder("39698370", "37500488");
    }
    @Test
    @DisplayName("findByBajaFalse no incluye personas con baja=true")
    void findByBajaFalse_noIncluyeDadosDeBaja() {
        List<Persona> activas = personaRepositorio.findByBajaFalse();        
        assertThat(activas).extracting(Persona::getDocumento).doesNotContain("00000000");
    }
    @Test
    @DisplayName("findByBajaFalse retorna lista vacia si todos estan dados de baja")
    void findByBajaFalse_listaVaciaSiTodosInactivos() {
        personaRepositorio.deleteAll();
        personaRepositorio.save(Persona.builder()
            .documento("00000000")
            .apellidos("Apellidos")
            .nombres("Nombres")
            .email("null@alms.com")
            .telefono("0000000000")
            .domicilio("null 000")
            .baja(true)
            .build());
        List<Persona> activas = personaRepositorio.findByBajaFalse();
        assertThat(activas).isEmpty();
    }

    @Test
    @DisplayName("findByPersonaIdAndBajaFalse retorna la persona si está activo")
    void findByPersonaIdAndBajaFalse_retornaActivo() {
        Optional<Persona> resultado = personaRepositorio.findByPersonaIdAndBajaFalse(ignacio.getPersonaId());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDocumento()).isEqualTo("39698370");
    }
    @Test
    @DisplayName("findByPersonaIdAndBajaFalse retorna vacío si la persona está dada de baja")
    void findByPersonaIdAndBajaFalse_retornaVacioSiDadoDeBaja() {
        Optional<Persona> resultado = personaRepositorio.findByPersonaIdAndBajaFalse(dadaDeBaja.getPersonaId());
        assertThat(resultado).isEmpty();
    }
    @Test
    @DisplayName("findByPersonaIdAndBajaFalse retorna vacío si el UUID no existe")
    void findByPersonaIdAndBajaFalse_retornaVacioSiNoExiste() {
        Optional<Persona> resultado = personaRepositorio.findByPersonaIdAndBajaFalse(UUID.randomUUID());
        assertThat(resultado).isEmpty();
    }
}
