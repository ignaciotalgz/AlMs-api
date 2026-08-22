package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
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

import com.algz.alms.dtos.asignatura.AsignaturaRequestDTO;
import com.algz.alms.dtos.asignatura.AsignaturaResponseDTO;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.AsignaturaServicio;

import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = AsignaturaControlador.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class AsignaturaControladorTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AsignaturaServicio asignaturaServicio;

    private UUID asignaturaId;
    private AsignaturaResponseDTO asignaturaResponseDTO;

    @BeforeEach
    void setUp() {
        asignaturaId = UUID.randomUUID();
        asignaturaResponseDTO = new AsignaturaResponseDTO(asignaturaId, "Algebra I", "MAT-101", false);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I", "MAT-101");
        when(asignaturaServicio.crear(any(AsignaturaRequestDTO.class))).thenReturn(asignaturaResponseDTO);

        mockMvc.perform(post("/api/v1/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("MAT-101"));
    }

    @Test
    void crear_codigoVacio_devuelve400() throws Exception {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I", "");

        mockMvc.perform(post("/api/v1/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_codigoDuplicado_devuelve400() throws Exception {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I", "MAT-101");
        when(asignaturaServicio.crear(any(AsignaturaRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Ya existe una asignatura con el codigo MAT-101"));

        mockMvc.perform(post("/api/v1/asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarTodas_devuelve200() throws Exception {
        when(asignaturaServicio.listarTodas(false)).thenReturn(List.of(asignaturaResponseDTO));

        mockMvc.perform(get("/api/v1/asignaturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(asignaturaServicio.obtenerPorId(asignaturaId))
                .thenThrow(new AsignaturaNoEncontradaException("No se encontro la asignatura con id " + asignaturaId));

        mockMvc.perform(get("/api/v1/asignaturas/{asignaturaId}", asignaturaId))
                .andExpect(status().isNotFound());
    }

    @Test
    void darDeBaja_devuelve204() throws Exception {
        doNothing().when(asignaturaServicio).darDeBaja(asignaturaId);

        mockMvc.perform(patch("/api/v1/asignaturas/{asignaturaId}/baja", asignaturaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivar_devuelve204() throws Exception {
        doNothing().when(asignaturaServicio).reactivar(asignaturaId);

        mockMvc.perform(patch("/api/v1/asignaturas/{asignaturaId}/reactivar", asignaturaId))
                .andExpect(status().isNoContent());
    }
}
