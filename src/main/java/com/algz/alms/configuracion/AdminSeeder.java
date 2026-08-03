package com.algz.alms.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.repositorios.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.nombre:Administrador}")
    private String adminNombre;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.existsByRol(Rol.ROLE_ADMIN)) {
            log.debug("Ya existe un usuario ROLE_ADMIN, se omite el seed inicial");
            return;
        }
        Usuario admin = Usuario.builder()
            .nombre(adminNombre)
            .email(adminEmail)
            .password(passwordEncoder.encode(adminPassword))
            .rol(Rol.ROLE_ADMIN)
            .persona(null)
            .build();
        usuarioRepositorio.save(admin);
        log.info("Usuario ROLE_ADMIN sembrado automáticamente con email: {}", adminEmail);
    }
}
