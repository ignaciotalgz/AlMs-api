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

import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaRequestDTO;
import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaResponseDTO;
import com.algz.alms.enumeraciones.TipoCorrelatividad;
import com.algz.alms.excepciones.ModuloAsignaturaNoEncontradaException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.AsignaturaCorrelativaServicio;

import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = AsignaturaCorrelativaControlador.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class AsignaturaCorrelativaControladorTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AsignaturaCorrelativaServicio asignaturaCorrelativaServicio;

    private UUID correlativaId;
    private UUID moduloAsignaturaId;
    private UUID moduloAsignaturaRequeridaId;
    private AsignaturaCorrelativaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        correlativaId = UUID.randomUUID();
        moduloAsignaturaId = UUID.randomUUID();
        moduloAsignaturaRequeridaId = UUID.randomUUID();
        responseDTO = new AsignaturaCorrelativaResponseDTO(
                correlativaId, moduloAsignaturaId, "Analisis II", moduloAsignaturaRequeridaId, "Algebra I",
                TipoCorrelatividad.CURSAR_REGULARIZADA, false);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_REGULARIZADA);
        when(asignaturaCorrelativaServicio.crear(any(AsignaturaCorrelativaRequestDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/asignatura-correlativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCorrelatividad").value("CURSAR_REGULARIZADA"));
    }

    @Test
    void crear_tipoNulo_devuelve400() throws Exception {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, null);

        mockMvc.perform(post("/api/v1/asignatura-correlativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_autoreferencia_devuelve400() throws Exception {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaId, TipoCorrelatividad.CURSAR_APROBADA);
        when(asignaturaCorrelativaServicio.crear(any(AsignaturaCorrelativaRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Una asignatura no puede ser correlativa de si misma"));

        mockMvc.perform(post("/api/v1/asignatura-correlativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_moduloAsignaturaInexistente_devuelve404() throws Exception {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_APROBADA);
        when(asignaturaCorrelativaServicio.crear(any(AsignaturaCorrelativaRequestDTO.class)))
                .thenThrow(new ModuloAsignaturaNoEncontradaException(
                        "No se encontro la relacion modulo-asignatura con id " + moduloAsignaturaId));

        mockMvc.perform(post("/api/v1/asignatura-correlativas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorModuloAsignatura_devuelve200() throws Exception {
        when(asignaturaCorrelativaServicio.listarPorModuloAsignatura(moduloAsignaturaId, false))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/asignatura-correlativas/modulo-asignatura/{id}", moduloAsignaturaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void darDeBaja_devuelve204() throws Exception {
        doNothing().when(asignaturaCorrelativaServicio).darDeBaja(correlativaId);

        mockMvc.perform(patch("/api/v1/asignatura-correlativas/{id}/baja", correlativaId))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivar_devuelve204() throws Exception {
        doNothing().when(asignaturaCorrelativaServicio).reactivar(correlativaId);

        mockMvc.perform(patch("/api/v1/asignatura-correlativas/{id}/reactivar", correlativaId))
                .andExpect(status().isNoContent());
    }
}
