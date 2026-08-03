package com.algz.alms.servicios;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.persona.PersonaRequest;
import com.algz.alms.dtos.persona.PersonaResponse;
import com.algz.alms.entidades.Persona;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.PersonaNoEncontradaException;
import com.algz.alms.repositorios.PersonaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonaServicio {
    private final PersonaRepositorio personaRepositorio;
    
    @Transactional(readOnly = true)
    public List<PersonaResponse> listar() {
        return personaRepositorio.findByBajaFalse().stream().map(PersonaResponse::of).toList();
    }
    @Transactional(readOnly = true)
    public PersonaResponse obtenerPorId(UUID personaId) {
        return PersonaResponse.of(obtenerActivoOLanzar(personaId));
    }

    @Transactional
    public PersonaResponse crear(PersonaRequest request) {
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
        return PersonaResponse.of(persona);
    }
    @Transactional
    public PersonaResponse actualizar(UUID personaId, PersonaRequest request){
        Persona persona = obtenerActivoOLanzar(personaId);
        if (!persona.getDocumento().equals(request.documento()) && personaRepositorio.existsByDocumento(request.documento())) {
            throw new PersonaDuplicadaException("Ya existe una persona registrada con el documento: " + request.documento());
        }
        persona.setDocumento(request.documento());
        persona.setApellidos(request.apellidos());
        persona.setNombres(request.nombres());
        persona.setEmail(request.email());
        persona.setTelefono(request.telefono());
        persona.setDomicilio(request.domicilio());
        personaRepositorio.save(persona);
        return PersonaResponse.of(persona);
    }
    @Transactional
    public void darDeBaja(UUID personaId) {
        Persona persona = obtenerActivoOLanzar(personaId);
        persona.setBaja(true);
        personaRepositorio.save(persona);
    }

    private Persona obtenerActivoOLanzar(UUID personaId){
        return personaRepositorio.findByPersonaIdAndBajaFalse(personaId).orElseThrow(() -> new PersonaNoEncontradaException("Persona no encontrada con id: " + personaId));
    }
}
