package com.algz.alms.servicios;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.usuario.AuthResponse;
import com.algz.alms.dtos.usuario.LoginRequest;
import com.algz.alms.dtos.usuario.RegistroRequest;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.excepciones.UsuarioDuplicadoException;
import com.algz.alms.repositorios.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServicio {
    private final UsuarioRepositorio usuarioRepositorio;
    private final JwtServicio jwtServicio;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authConfig;

    private AuthenticationManager getAuthManager() {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener el AuthenticationManager", e);
        }
    }

    @Transactional
    public AuthResponse registro(RegistroRequest request){
        if(usuarioRepositorio.existsByEmail(request.email())){
            throw new UsuarioDuplicadoException("El email ya esta registrado: " + request.email());
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(Rol.ROLE_USER);
        usuarioRepositorio.save(usuario);

        String token = jwtServicio.generateToken(Map.of("rol", usuario.getRol().name()), usuario);
        return AuthResponse.of(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().name());
    }

    public AuthResponse login(LoginRequest request){
        getAuthManager().authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        Usuario usuario = usuarioRepositorio.findByEmail(request.email()).orElseThrow(() -> new IllegalStateException("Usuario no encontrado tras autenticacion exitosa"));
        String token = jwtServicio.generateToken(Map.of("rol", usuario.getRol().name()), usuario);
        return AuthResponse.of(token, usuario.getNombre(), usuario.getEmail(), usuario.getRol().name());
    }
}
