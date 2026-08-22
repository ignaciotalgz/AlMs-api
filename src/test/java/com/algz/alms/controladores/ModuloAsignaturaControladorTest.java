package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaRequestDTO;
import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaResponseDTO;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.ModuloAsignaturaServicio;

import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = ModuloAsignaturaControlador.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class ModuloAsignaturaControladorTest {
    @Autowired
    private MockMvc mockMvc;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
    private ModuloAsignaturaServicio moduloAsignaturaServicio;

    private UUID moduloAsignaturaId;
    private UUID moduloId;
    private UUID asignaturaId;
    private ModuloAsignaturaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        moduloAsignaturaId = UUID.randomUUID();
        moduloId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();
        responseDTO = new ModuloAsignaturaResponseDTO(
                moduloAsignaturaId, moduloId, "Ciclo Basico", asignaturaId, "Algebra I", "MAT-101", 1, false);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloAsignaturaServicio.crear(any(ModuloAsignaturaRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/modulo-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.asignaturaCodigo").value("MAT-101"));
    }

    @Test
    void crear_asignaturaInexistente_devuelve404() throws Exception {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloAsignaturaServicio.crear(any(ModuloAsignaturaRequestDTO.class)))
                .thenThrow(new AsignaturaNoEncontradaException("No se encontro la asignatura con id " + asignaturaId));

        mockMvc.perform(post("/api/v1/modulo-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_asignaturaYaEnModulo_devuelve400() throws Exception {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloAsignaturaServicio.crear(any(ModuloAsignaturaRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Esta asignatura ya esta agregada a este modulo"));

        mockMvc.perform(post("/api/v1/modulo-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPorModulo_moduloInexistente_devuelve404() throws Exception {
        when(moduloAsignaturaServicio.listarPorModulo(moduloId, false))
                .thenThrow(new ModuloNoEncontradoException("No se encontro el modulo con id " + moduloId));

        mockMvc.perform(get("/api/v1/modulo-asignaturas/modulo/{moduloId}", moduloId))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorModulo_devuelve200() throws Exception {
        when(moduloAsignaturaServicio.listarPorModulo(moduloId, false)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/modulo-asignaturas/modulo/{moduloId}", moduloId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void actualizarOrden_datosValidos_devuelve200() throws Exception {
        ModuloAsignaturaResponseDTO actualizado = new ModuloAsignaturaResponseDTO(
                moduloAsignaturaId, moduloId, "Ciclo Basico", asignaturaId, "Algebra I", "MAT-101", 3, false);
        when(moduloAsignaturaServicio.actualizarOrden(eq(moduloAsignaturaId), eq(3))).thenReturn(actualizado);

        mockMvc.perform(patch("/api/v1/modulo-asignaturas/{id}/orden", moduloAsignaturaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orden\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orden").value(3));
    }

    @Test
    void actualizarOrden_ordenInvalido_devuelve400() throws Exception {
        mockMvc.perform(patch("/api/v1/modulo-asignaturas/{id}/orden", moduloAsignaturaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orden\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void darDeBaja_devuelve204() throws Exception {
        doNothing().when(moduloAsignaturaServicio).darDeBaja(moduloAsignaturaId);

        mockMvc.perform(patch("/api/v1/modulo-asignaturas/{id}/baja", moduloAsignaturaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivar_devuelve204() throws Exception {
        doNothing().when(moduloAsignaturaServicio).reactivar(moduloAsignaturaId);

        mockMvc.perform(patch("/api/v1/modulo-asignaturas/{id}/reactivar", moduloAsignaturaId))
                .andExpect(status().isNoContent());
    }
}
