package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaRequestDTO;
import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaResponseDTO;
import com.algz.alms.enumeraciones.Cargo;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.DocenteAsignaturaServicio;

import tools.jackson.databind.ObjectMapper;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = DocenteAsignaturaControlador.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class DocenteAsignaturaControladorTest {
    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DocenteAsignaturaServicio docenteAsignaturaServicio;

    private UUID docenteAsignaturaId;
    private UUID docenteId;
    private UUID asignaturaId;
    private DocenteAsignaturaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        docenteAsignaturaId = UUID.randomUUID();
        docenteId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();
        responseDTO = new DocenteAsignaturaResponseDTO(
                docenteAsignaturaId, docenteId, "LEG-001", asignaturaId, "Algebra I",
                Cargo.TITULAR, LocalDate.of(2024, 3, 1), null, false, null);
    }

    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteAsignaturaServicio.crear(any(DocenteAsignaturaRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/docente-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.docenteLegajo").value("LEG-001"));
    }

    @Test
    void crear_cargoNulo_devuelve400() throws Exception {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, null, LocalDate.of(2024, 3, 1), null);

        mockMvc.perform(post("/api/v1/docente-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_docenteInexistente_devuelve404() throws Exception {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteAsignaturaServicio.crear(any(DocenteAsignaturaRequestDTO.class)))
                .thenThrow(new DocenteNoEncontradoException("No se encontro el docente con id " + docenteId));

        mockMvc.perform(post("/api/v1/docente-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_asignacionDuplicada_devuelve400() throws Exception {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteAsignaturaServicio.crear(any(DocenteAsignaturaRequestDTO.class)))
                .thenThrow(new IllegalArgumentException(
                        "Este docente ya tiene una asignacion activa con ese cargo en esta asignatura"));

        mockMvc.perform(post("/api/v1/docente-asignaturas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarPorAsignatura_devuelve200() throws Exception {
        when(docenteAsignaturaServicio.listarPorAsignatura(asignaturaId, false)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/docente-asignaturas/asignatura/{asignaturaId}", asignaturaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listarPorDocente_devuelve200() throws Exception {
        when(docenteAsignaturaServicio.listarPorDocente(docenteId, false)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/docente-asignaturas/docente/{docenteId}", docenteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void darDeBaja_devuelve204() throws Exception {
        doNothing().when(docenteAsignaturaServicio).darDeBaja(docenteAsignaturaId);

        mockMvc.perform(patch("/api/v1/docente-asignaturas/{id}/baja", docenteAsignaturaId))
                .andExpect(status().isNoContent());
    }
}
