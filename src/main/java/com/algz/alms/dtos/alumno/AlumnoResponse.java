package com.algz.alms.dtos.alumno;

import java.time.LocalDate;
import java.util.UUID;

import com.algz.alms.entidades.Alumno;
import com.algz.alms.entidades.Persona;

public record AlumnoResponse(UUID personaId,
    String documento,
    String apellidos,
    String nombres,
    String email,
    String telefono,
    String domicilio,
    String legajo,
    LocalDate fechaAlta) {
public static AlumnoResponse of(Alumno alumno) {
        Persona persona = alumno.getPersona();
        return new AlumnoResponse(
            persona.getPersonaId(),
            persona.getDocumento(),
            persona.getApellidos(),
            persona.getNombres(),
            persona.getEmail(),
            persona.getTelefono(),
            persona.getDomicilio(),
            alumno.getLegajo(),
            alumno.getFechaAlta()
        );
    }
}
