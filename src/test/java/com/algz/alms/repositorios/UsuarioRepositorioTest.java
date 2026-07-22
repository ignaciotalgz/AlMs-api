package com.algz.alms.repositorios;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@DisplayName("UsuarioRepositorio")
public class UsuarioRepositorioTest {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @BeforeEach
    void setUp() {
        usuarioRepositorio.deleteAll();

        usuarioRepositorio.save(Usuario.builder()
                .nombre("Ignacio")
                .email("ignacio@iamvita.com")
                .password("hash-no-importa-en-este-nivel")
                .rol(Rol.ROLE_USER)
                .build());
    }
    @Test
    @DisplayName("findByEmail retorna el usuario cuando el email existe")
    void findByEmail_retornaUsuarioCuandoExiste() {
        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("ignacio@iamvita.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Ignacio");
        assertThat(resultado.get().getRol()).isEqualTo(Rol.ROLE_USER);
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
        Optional<Usuario> resultado = usuarioRepositorio.findByEmail("IGNACIO@IAMVITA.COM");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail retorna true cuando el email ya está registrado")
    void existsByEmail_retornaTrueCuandoExiste() {
        boolean existe = usuarioRepositorio.existsByEmail("ignacio@iamvita.com");

        assertThat(existe).isTrue();
    }
    @Test
    @DisplayName("existsByEmail retorna false cuando el email no está registrado")
    void existsByEmail_retornaFalseCuandoNoExiste() {
        boolean existe = usuarioRepositorio.existsByEmail("nuevo@iamvita.com");

        assertThat(existe).isFalse();
    }
}
