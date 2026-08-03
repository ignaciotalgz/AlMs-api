package com.algz.alms.servicios;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.alumno.AlumnoRegistroRequest;
import com.algz.alms.dtos.alumno.AlumnoResponse;
import com.algz.alms.entidades.Alumno;
import com.algz.alms.entidades.Persona;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.excepciones.AlumnoNoEncontradoException;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.UsuarioDuplicadoException;
import com.algz.alms.repositorios.AlumnoRepositorio;
import com.algz.alms.repositorios.PersonaRepositorio;
import com.algz.alms.repositorios.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlumnoServicio {
    private static final String PREFIJO_LEGAJO = "AL";
    private final AlumnoRepositorio alumnoRepositorio;
    private final PersonaRepositorio personaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final InvitacionServicio invitacionServicio;

    @Transactional(readOnly = true)
    public List<AlumnoResponse> listar() {
        return alumnoRepositorio.findActivos().stream().map(AlumnoResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public AlumnoResponse obtenerPorId(UUID personaId) {
        return AlumnoResponse.of(obtenerActivoOLanzar(personaId));
    }

    @Transactional
    public AlumnoResponse registrar(AlumnoRegistroRequest request) {
        invitacionServicio.validarYConsumir(request.token(), Rol.ROLE_ALUMNO);

        if (usuarioRepositorio.existsByEmail(request.email())) {
            throw new UsuarioDuplicadoException("Ya existe un usuario registrado con el email: " + request.email());
        }
        if (personaRepositorio.existsByDocumento(request.documento())) {
            throw new PersonaDuplicadaException("Ya existe una persona registrada con el documento: " + request.documento());
        }

        Persona persona = Persona.builder()
            .documento(request.documento())
            .apellidos(request.apellidos())
            .nombres(request.nombres())
            .email(request.email())
            .telefono(request.telefono())
            .domicilio(request.domicilio())
            .baja(false)
            .build();
        personaRepositorio.save(persona);

        Alumno alumno = Alumno.builder()
            .persona(persona)
            .legajo(generarLegajo())
            .fechaAlta(LocalDate.now())
            .build();
        alumnoRepositorio.save(alumno);

        Usuario usuario = Usuario.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .rol(Rol.ROLE_ALUMNO)
            .persona(persona)
            .build();
        usuarioRepositorio.save(usuario);

        return AlumnoResponse.of(alumno);
    }

    private Alumno obtenerActivoOLanzar(UUID personaId) {
        return alumnoRepositorio.findActivoPorPersonaId(personaId)
            .orElseThrow(() -> new AlumnoNoEncontradoException("Alumno no encontrado con id: " + personaId));
    }
    private String generarLegajo() {
        long siguiente = alumnoRepositorio.count() + 1;
        return PREFIJO_LEGAJO + "-" + Year.now() + "-" + String.format("%04d", siguiente);
    }
}
