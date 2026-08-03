package com.algz.alms.configuracion;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminSeeder")
public class AdminSeederTest {
    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeeder adminSeeder;

    private void configurarPropiedades() {
        ReflectionTestUtils.setField(adminSeeder, "adminNombre", "Administrador Test");
        ReflectionTestUtils.setField(adminSeeder, "adminEmail", "admin@alms.com");
        ReflectionTestUtils.setField(adminSeeder, "adminPassword", "ClaveSegura123");
    }

    @Test
    @DisplayName("crea el usuario ROLE_ADMIN cuando todavía no existe ninguno")
    void run_creaAdminCuandoNoExiste() {
        configurarPropiedades();
        when(usuarioRepositorio.existsByRol(Rol.ROLE_ADMIN)).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("hash-encriptado");

        adminSeeder.run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepositorio).save(captor.capture());

        Usuario admin = captor.getValue();
        assertThat(admin.getEmail()).isEqualTo("admin@alms.com");
        assertThat(admin.getNombre()).isEqualTo("Administrador Test");
        assertThat(admin.getPassword()).isEqualTo("hash-encriptado");
        assertThat(admin.getRol()).isEqualTo(Rol.ROLE_ADMIN);
        assertThat(admin.getPersona()).isNull();
    }

    @Test
    @DisplayName("no crea un admin nuevo si ya existe uno")
    void run_noCreaAdminSiYaExiste() {
        when(usuarioRepositorio.existsByRol(Rol.ROLE_ADMIN)).thenReturn(true);

        adminSeeder.run(null);

        verify(usuarioRepositorio, never()).save(org.mockito.ArgumentMatchers.any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
