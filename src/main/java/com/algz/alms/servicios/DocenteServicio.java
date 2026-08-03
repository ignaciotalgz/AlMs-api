package com.algz.alms.servicios;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.docente.DocenteRegistroRequest;
import com.algz.alms.dtos.docente.DocenteResponse;
import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.Persona;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.UsuarioDuplicadoException;
import com.algz.alms.repositorios.DocenteRepositorio;
import com.algz.alms.repositorios.PersonaRepositorio;
import com.algz.alms.repositorios.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocenteServicio {
    private static final String PREFIJO_LEGAJO = "DO";

    private final DocenteRepositorio docenteRepositorio;
    private final PersonaRepositorio personaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final InvitacionServicio invitacionServicio;

    @Transactional(readOnly = true)
    public List<DocenteResponse> listar() {
        return docenteRepositorio.findActivos().stream().map(DocenteResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public DocenteResponse obtenerPorId(UUID personaId) {
        return DocenteResponse.of(obtenerActivoOLanzar(personaId));
    }

    @Transactional
    public DocenteResponse registrar(DocenteRegistroRequest request) {
        invitacionServicio.validarYConsumir(request.token(), Rol.ROLE_DOCENTE);

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

        Docente docente = Docente.builder()
            .persona(persona)
            .legajo(generarLegajo())
            .fechaAlta(LocalDate.now())
            .build();
        docenteRepositorio.save(docente);

        Usuario usuario = Usuario.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .rol(Rol.ROLE_DOCENTE)
            .persona(persona)
            .build();
        usuarioRepositorio.save(usuario);

        return DocenteResponse.of(docente);
    }

    private Docente obtenerActivoOLanzar(UUID personaId) {
        return docenteRepositorio.findActivoPorPersonaId(personaId)
            .orElseThrow(() -> new DocenteNoEncontradoException("Docente no encontrado con id: " + personaId));
    }

    private String generarLegajo() {
        long siguiente = docenteRepositorio.count() + 1;
        return PREFIJO_LEGAJO + "-" + Year.now() + "-" + String.format("%04d", siguiente);
    }
}
