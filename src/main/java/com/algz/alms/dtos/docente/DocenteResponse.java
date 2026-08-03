package com.algz.alms.dtos.docente;

import java.time.LocalDate;
import java.util.UUID;

import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.Persona;

public record DocenteResponse(UUID personaId,
    String documento,
    String apellidos,
    String nombres,
    String email,
    String telefono,
    String domicilio,
    String legajo,
    LocalDate fechaAlta) {
public static DocenteResponse of(Docente docente) {
        Persona persona = docente.getPersona();
        return new DocenteResponse(
            persona.getPersonaId(),
            persona.getDocumento(),
            persona.getApellidos(),
            persona.getNombres(),
            persona.getEmail(),
            persona.getTelefono(),
            persona.getDomicilio(),
            docente.getLegajo(),
            docente.getFechaAlta()
        );
    }
}
