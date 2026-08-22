package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

import com.algz.alms.dtos.modulo.ModuloRequestDTO;
import com.algz.alms.dtos.modulo.ModuloResponseDTO;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.ModuloServicio;

import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = ModuloControlador.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class ModuloControladorTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ModuloServicio moduloServicio;

    private UUID moduloId;
    private UUID planEstudioId;
    private ModuloResponseDTO moduloResponseDTO;

    @BeforeEach
    void setUp() {
        moduloId = UUID.randomUUID();
        planEstudioId = UUID.randomUUID();
        moduloResponseDTO = new ModuloResponseDTO(
                moduloId, planEstudioId, "Plan 2024", "Ciclo Basico", 1, false);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 1);
        when(moduloServicio.crear(any(ModuloRequestDTO.class))).thenReturn(moduloResponseDTO);

        mockMvc.perform(post("/api/v1/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moduloId").value(moduloId.toString()))
                .andExpect(jsonPath("$.orden").value(1));
    }

    @Test
    void crear_ordenNulo_devuelve400() throws Exception {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", null);

        mockMvc.perform(post("/api/v1/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_ordenNoPositivo_devuelve400() throws Exception {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 0);

        mockMvc.perform(post("/api/v1/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_planInexistente_devuelve404() throws Exception {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 1);
        when(moduloServicio.crear(any(ModuloRequestDTO.class)))
                .thenThrow(new PlanEstudioNoEncontradoException("No se encontro el plan de estudio con id " + planEstudioId));

        mockMvc.perform(post("/api/v1/modulos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorPlan_devuelve200() throws Exception {
        when(moduloServicio.listarPorPlan(planEstudioId, false)).thenReturn(List.of(moduloResponseDTO));

        mockMvc.perform(get("/api/v1/modulos/plan/{planEstudioId}", planEstudioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(moduloServicio.obtenerPorId(moduloId))
                .thenThrow(new ModuloNoEncontradoException("No se encontro el modulo con id " + moduloId));

        mockMvc.perform(get("/api/v1/modulos/{moduloId}", moduloId))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_ordenDuplicado_devuelve400() throws Exception {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 2);
        when(moduloServicio.actualizar(eq(moduloId), any(ModuloRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un modulo con el orden 2 en este plan de estudio"));

        mockMvc.perform(put("/api/v1/modulos/{moduloId}", moduloId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void darDeBaja_devuelve204() throws Exception {
        doNothing().when(moduloServicio).darDeBaja(moduloId);

        mockMvc.perform(patch("/api/v1/modulos/{moduloId}/baja", moduloId))
                .andExpect(status().isNoContent());

        verify(moduloServicio).darDeBaja(moduloId);
    }

    @Test
    void reactivar_devuelve204() throws Exception {
        doNothing().when(moduloServicio).reactivar(moduloId);

        mockMvc.perform(patch("/api/v1/modulos/{moduloId}/reactivar", moduloId))
                .andExpect(status().isNoContent());
    }
}
