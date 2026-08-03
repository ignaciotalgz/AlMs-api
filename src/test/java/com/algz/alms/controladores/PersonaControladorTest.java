package com.algz.alms.controladores;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.algz.alms.dtos.persona.PersonaRequest;
import com.algz.alms.dtos.persona.PersonaResponse;
import com.algz.alms.excepciones.PersonaDuplicadaException;
import com.algz.alms.excepciones.PersonaNoEncontradaException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.PersonaServicio;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = PersonaControlador.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)

)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PersonaControlador")
class PersonaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    // @Autowired
    // private ObjectMapper objectMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PersonaServicio personaServicio;

    private UUID personaId;
    private PersonaResponse response;

    @BeforeEach
    void setUp() {
        personaId = UUID.randomUUID();
        response = new PersonaResponse(personaId, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
    }

    @Test
    @DisplayName("GET /api/v1/personas retorna 200 y la lista")
    void listar_retorna200() throws Exception {
        when(personaServicio.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/personas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].documento").value("39698370"));
    }

    @Test
    @DisplayName("GET /api/v1/personas/{id} retorna 200 cuando existe")
    void obtener_retorna200CuandoExiste() throws Exception {
        when(personaServicio.obtenerPorId(personaId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/personas/{id}", personaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombres").value("Ignacio Tomás"));
    }

    @Test
    @DisplayName("GET /api/v1/personas/{id} retorna 404 cuando no existe")
    void obtener_retorna404CuandoNoExiste() throws Exception {
        when(personaServicio.obtenerPorId(personaId)).thenThrow(new PersonaNoEncontradaException("Persona no encontrada con id: " + personaId));

        mockMvc.perform(get("/api/v1/personas/{id}", personaId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/personas retorna 201 cuando se crea correctamente")
    void crear_retorna201() throws Exception {
        PersonaRequest request = new PersonaRequest("39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
        when(personaServicio.crear(any(PersonaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.documento").value("39698370"));
    }

    @Test
    @DisplayName("POST /api/v1/personas retorna 409 cuando el documento ya existe")
    void crear_retorna409CuandoDocumentoDuplicado() throws Exception {
        PersonaRequest request = new PersonaRequest("39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
        when(personaServicio.crear(any(PersonaRequest.class)))
            .thenThrow(new PersonaDuplicadaException("Ya existe una persona registrada con el documento: 39698370"));

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/personas retorna 400 cuando el email es inválido")
    void crear_retorna400CuandoEmailInvalido() throws Exception {
        PersonaRequest request = new PersonaRequest("39698370", "Alvarez Gonzalez", "Ignacio Tomás", "no-es-un-email", "3813527690", "Don bosco 2579");

        mockMvc.perform(post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/personas/{id} retorna 200 con los datos actualizados")
    void actualizar_retorna200() throws Exception {
        PersonaRequest request = new PersonaRequest("39698370", "Nuevo Apellido", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
        PersonaResponse actualizado = new PersonaResponse(personaId, "39698370", "Nuevo Apellido", "Ignacio Tomás", "ignacio@alms.com", "3813527690", "Don bosco 2579");
        when(personaServicio.actualizar(eq(personaId), any(PersonaRequest.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/personas/{id}", personaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.apellidos").value("Nuevo Apellido"));
    }

    @Test
    @DisplayName("PATCH /api/v1/personas/{id}/baja retorna 204")
    void darDeBaja_retorna204() throws Exception {
        doNothing().when(personaServicio).darDeBaja(personaId);

        mockMvc.perform(patch("/api/v1/personas/{id}/baja", personaId))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/personas/{id}/baja retorna 404 si no existe")
    void darDeBaja_retorna404SiNoExiste() throws Exception {
        doThrow(new PersonaNoEncontradaException("Persona no encontrada con id: " + personaId))
            .when(personaServicio).darDeBaja(personaId);

        mockMvc.perform(patch("/api/v1/personas/{id}/baja", personaId))
            .andExpect(status().isNotFound());
    }
}