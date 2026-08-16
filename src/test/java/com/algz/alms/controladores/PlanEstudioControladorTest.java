package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.algz.alms.dtos.planestudio.PlanEstudioRequestDTO;
import com.algz.alms.dtos.planestudio.PlanEstudioResponseDTO;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.PlanEstudioServicio;

import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;


@WebMvcTest(controllers = PlanEstudioControlador.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class PlanEstudioControladorTest {
    @Autowired
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PlanEstudioServicio planEstudioServicio;

    private UUID planEstudioId;
    private UUID carreraId;
    private PlanEstudioResponseDTO planEstudioResponseDTO;

    @BeforeEach
    void setUp() {
        planEstudioId = UUID.randomUUID();
        carreraId = UUID.randomUUID();
        planEstudioResponseDTO = new PlanEstudioResponseDTO(
                planEstudioId, carreraId, "Ingenieria en Sistemas",
                "Plan 2024", LocalDate.of(2024, 3, 1), null, false);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2024", LocalDate.of(2024, 3, 1), null);
        when(planEstudioServicio.crear(any(PlanEstudioRequestDTO.class))).thenReturn(planEstudioResponseDTO);

        mockMvc.perform(post("/api/v1/planes-estudio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planEstudioId").value(planEstudioId.toString()))
                .andExpect(jsonPath("$.nombre").value("Plan 2024"));
    }
    @Test
    void crear_carreraInexistente_devuelve404() throws Exception {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2024", LocalDate.of(2024, 3, 1), null);
        when(planEstudioServicio.crear(any(PlanEstudioRequestDTO.class)))
                .thenThrow(new CarreraNoEncontradaException("No se encontro la carrera con id " + carreraId));

        mockMvc.perform(post("/api/v1/planes-estudio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    @Test
    void crear_nombreVacio_devuelve400() throws Exception {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "", LocalDate.of(2024, 3, 1), null);

        mockMvc.perform(post("/api/v1/planes-estudio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void crear_carreraIdNula_devuelve400() throws Exception {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                null, "Plan 2024", LocalDate.of(2024, 3, 1), null);

        mockMvc.perform(post("/api/v1/planes-estudio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void listarTodos_devuelve200() throws Exception {
        when(planEstudioServicio.listarTodos(false)).thenReturn(List.of(planEstudioResponseDTO));

        mockMvc.perform(get("/api/v1/planes-estudio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
    @Test
    void listarPorCarrera_devuelve200() throws Exception {
        when(planEstudioServicio.listarPorCarrera(carreraId, false)).thenReturn(List.of(planEstudioResponseDTO));

        mockMvc.perform(get("/api/v1/planes-estudio/carrera/{carreraId}", carreraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].carreraId").value(carreraId.toString()));
    }

    @Test
    void listarPorCarrera_carreraInexistente_devuelve404() throws Exception {
        when(planEstudioServicio.listarPorCarrera(carreraId, false))
                .thenThrow(new CarreraNoEncontradaException("No se encontro la carrera con id " + carreraId));

        mockMvc.perform(get("/api/v1/planes-estudio/carrera/{carreraId}", carreraId))
                .andExpect(status().isNotFound());
    }
    @Test
    void obtenerPorId_existente_devuelve200() throws Exception {
        when(planEstudioServicio.obtenerPorId(planEstudioId)).thenReturn(planEstudioResponseDTO);

        mockMvc.perform(get("/api/v1/planes-estudio/{planEstudioId}", planEstudioId))
                .andExpect(status().isOk());
    }
    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(planEstudioServicio.obtenerPorId(planEstudioId))
                .thenThrow(new PlanEstudioNoEncontradoException(
                        "No se encontro el plan de estudio con id " + planEstudioId));

        mockMvc.perform(get("/api/v1/planes-estudio/{planEstudioId}", planEstudioId))
                .andExpect(status().isNotFound());
    }
    @Test
    void actualizar_datosValidos_devuelve200() throws Exception {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2025", LocalDate.of(2025, 3, 1), null);
        PlanEstudioResponseDTO actualizado = new PlanEstudioResponseDTO(
                planEstudioId, carreraId, "Ingenieria en Sistemas",
                "Plan 2025", LocalDate.of(2025, 3, 1), null, false);
        when(planEstudioServicio.actualizar(eq(planEstudioId), any(PlanEstudioRequestDTO.class)))
                .thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/planes-estudio/{planEstudioId}", planEstudioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Plan 2025"));
    }
    @Test
    void darDeBaja_existente_devuelve204() throws Exception {
        doNothing().when(planEstudioServicio).darDeBaja(planEstudioId);

        mockMvc.perform(patch("/api/v1/planes-estudio/{planEstudioId}/baja", planEstudioId))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivar_existente_devuelve204() throws Exception {
        doNothing().when(planEstudioServicio).reactivar(planEstudioId);

        mockMvc.perform(patch("/api/v1/planes-estudio/{planEstudioId}/reactivar", planEstudioId))
                .andExpect(status().isNoContent());
    }
}
