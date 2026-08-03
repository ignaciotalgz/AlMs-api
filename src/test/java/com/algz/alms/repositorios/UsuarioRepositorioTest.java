package com.algz.alms.repositorios;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.algz.alms.entidades.Persona;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@DisplayName("UsuarioRepositorio")
public class UsuarioRepositorioTest {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PersonaRepositorio personaRepositorio;

    @BeforeEach
    void setUp() {
        usuarioRepositorio.deleteAll();
        personaRepositorio.deleteAll();

        Persona ignacioPersona = personaRepositorio.save(Persona.builder()
                .documento("39698370")
                .apellidos("Alvarez Gonzalez")
                .nombres("Ignacio Tomás")
                .email("agit@alms.com")
                .telefono("3813527690")
                .domicilio("Don bosco 2579")
                .baja(false)
                .build());

        usuarioRepositorio.save(Usuario.builder()
                .nombre("Ignacio")
                .email("ignacio@alms.com")
                .password("hash-no-importa-en-este-nivel")
                .rol(Rol.ROLE_ALUMNO)
                .persona(ignacioPersona)
                .build());
    }
    @Test
    @DisplayName("findByEmail retorna el usuario cuando el email existe")
    void findByEmail_retornaUsuarioCuandoExiste() {
        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("ignacio@alms.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Ignacio");
        assertThat(resultado.get().getRol()).isEqualTo(Rol.ROLE_ALUMNO);
    }
    @Test
    @DisplayName("findByEmail retorna vacío cuando el email no existe")
    void findByEmail_retornaVacioCuandoNoExiste() {
        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("noexiste@mail.com");

        assertThat(resultado).isEmpty();
    }
    @Test
    @DisplayName("findByEmail es sensible a mayúsculas")
    void findByEmail_esCaseSensitive() {
        // PostgreSQL y H2 por defecto son case-sensitive en WHERE email = ?
        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("IGNACIO@ALMS.COM");
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail retorna true cuando el email ya está registrado")
    void existsByEmail_retornaTrueCuandoExiste() {
        boolean existe = usuarioRepositorio.existsByEmail("ignacio@alms.com");

        assertThat(existe).isTrue();
    }
    @Test
    @DisplayName("existsByEmail retorna false cuando el email no está registrado")
    void existsByEmail_retornaFalseCuandoNoExiste() {
        boolean existe = usuarioRepositorio.existsByEmail("nuevo@alms.com");

        assertThat(existe).isFalse();
    }

    @Test
    @DisplayName("existsByRol retorna false cuando ningún usuario tiene ese rol")
    void existsByRol_retornaFalseCuandoNoHayNinguno() {
        assertThat(usuarioRepositorio.existsByRol(Rol.ROLE_ADMIN)).isFalse();
    }
    @Test
    @DisplayName("existsByRol retorna true cuando existe un usuario con ese rol")
    void existsByRol_retornaTrueCuandoExiste() {
        assertThat(usuarioRepositorio.existsByRol(Rol.ROLE_ALUMNO)).isTrue();
    }

    @Test
    @DisplayName("un Usuario puede guardarse sin Persona asociada (caso admin)")
    void guardaUsuario_sinPersonaAsociada() {
        Usuario admin = usuarioRepositorio.save(Usuario.builder()
                .nombre("Administrador")
                .email("admin@alms.com")
                .password("hash")
                .rol(Rol.ROLE_ADMIN)
                .persona(null)
                .build());

        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("admin@alms.com");
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getPersona()).isNull();
        assertThat(admin.getNombreVisible()).isEqualTo("Administrador");
    }

    @Test
    @DisplayName("getNombreVisible antepone el nombre completo de la Persona cuando existe")
    void getNombreVisible_usaNombreDePersonaCuandoExiste() {
        Usuario usuario = usuarioRepositorio.findByEmail("ignacio@alms.com").orElseThrow();

        assertThat(usuario.getNombreVisible()).isEqualTo("Ignacio Tomás Alvarez Gonzalez");
    }
}
