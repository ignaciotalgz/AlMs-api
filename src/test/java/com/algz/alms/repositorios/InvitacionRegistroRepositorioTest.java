package com.algz.alms.repositorios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.algz.alms.entidades.InvitacionRegistro;
import com.algz.alms.enumeraciones.Rol;

@DataJpaTest
@DisplayName("InvitacionRegistroRepositorio")
public class InvitacionRegistroRepositorioTest {
    @Autowired
    private InvitacionRegistroRepositorio invitacionRegistroRepositorio;
    private InvitacionRegistro invitacion;

    @BeforeEach
    void setUp() {
        invitacionRegistroRepositorio.deleteAll();
        invitacion = invitacionRegistroRepositorio.save(InvitacionRegistro.builder()
            .tokenHash("hash-de-prueba-abc123")
            .rol(Rol.ROLE_ALUMNO)
            .usada(false)
            .fechaCreacion(LocalDateTime.now())
            .fechaExpiracion(LocalDateTime.now().plusHours(24))
            .build());
    }
    @Test
    @DisplayName("findByTokenHashParaActualizar retorna la invitación cuando el hash existe")
    void findByTokenHashParaActualizar_retornaCuandoExiste() {
        Optional<InvitacionRegistro> resultado = invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-de-prueba-abc123");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRol()).isEqualTo(Rol.ROLE_ALUMNO);
    }

    @Test
    @DisplayName("findByTokenHashParaActualizar retorna vacío cuando el hash no existe")
    void findByTokenHashParaActualizar_retornaVacioCuandoNoExiste() {
        Optional<InvitacionRegistro> resultado = invitacionRegistroRepositorio.findByTokenHashParaActualizar("hash-inexistente");

        assertThat(resultado).isEmpty();
    }
    @Test
    @DisplayName("findByIdParaActualizar retorna la invitación cuando el id existe")
    void findByIdParaActualizar_retornaCuandoExiste() {
        Optional<InvitacionRegistro> resultado = invitacionRegistroRepositorio.findByIdParaActualizar(invitacion.getInvitacionId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getTokenHash()).isEqualTo("hash-de-prueba-abc123");
    }

    @Test
    @DisplayName("findByIdParaActualizar retorna vacío cuando el id no existe")
    void findByIdParaActualizar_retornaVacioCuandoNoExiste() {
        Optional<InvitacionRegistro> resultado = invitacionRegistroRepositorio.findByIdParaActualizar(UUID.randomUUID());

        assertThat(resultado).isEmpty();
    }
}
