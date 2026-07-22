package com.algz.alms.dtos.persona;

import java.util.UUID;

import com.algz.alms.entidades.Persona;

public record PersonaResponse(UUID personaId, String documento, String apellidos, String nombres, String email, String telefono, String domicilio) {
    public static PersonaResponse of(Persona persona){
        return new PersonaResponse(persona.getPersonaId(), persona.getDocumento(), persona.getApellidos(), persona.getNombres(), persona.getEmail(), persona.getTelefono(), persona.getDomicilio());
    }
}
