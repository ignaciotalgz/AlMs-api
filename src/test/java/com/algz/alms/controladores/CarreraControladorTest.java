package com.algz.alms.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.algz.alms.dtos.carrera.CarreraRequestDTO;
import com.algz.alms.dtos.carrera.CarreraResponseDTO;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.CarreraServicio;

import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = CarreraControlador.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
public class CarreraControladorTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @MockitoBean
    private CarreraServicio carreraServicio;

    private UUID carreraId;
    private CarreraResponseDTO carreraResponseDTO;

    @BeforeEach
    void setUp() {
        carreraId = UUID.randomUUID();
        carreraResponseDTO = new CarreraResponseDTO(carreraId, "Ingenieria en Sistemas", false);
    }
    @Test
    void crear_datosValidos_devuelve201() throws Exception {
        CarreraRequestDTO request = new CarreraRequestDTO("Ingenieria en Sistemas");
        when(carreraServicio.crear(any(CarreraRequestDTO.class))).thenReturn(carreraResponseDTO);
        mockMvc.perform(post("/api/v1/carreras")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.carreraId").value(carreraId.toString()))
            .andExpect(jsonPath("$.nombre").value("Ingenieria en Sistemas"))
            .andExpect(jsonPath("$.baja").value(false));
        verify(carreraServicio).crear(any(CarreraRequestDTO.class));
    }

    @Test
    void crear_nombreVacio_devuelve400() throws Exception {
        CarreraRequestDTO request = new CarreraRequestDTO("");

        mockMvc.perform(post("/api/v1/carreras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(carreraServicio, never()).crear(any());
    }
    @Test
    void listarTodas_devuelve200ConLista() throws Exception {
        when(carreraServicio.listarTodas(false)).thenReturn(List.of(carreraResponseDTO));
        mockMvc.perform(get("/api/v1/carreras"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].nombre").value("Ingenieria en Sistemas"));
    }

    @Test
    void listarTodas_incluirBajas_pasaParametroAlServicio() throws Exception {
        when(carreraServicio.listarTodas(true)).thenReturn(List.of(carreraResponseDTO));

        mockMvc.perform(get("/api/v1/carreras").param("incluirBajas", "true"))
                .andExpect(status().isOk());

        verify(carreraServicio).listarTodas(true);
    }

    @Test
    void obtenerPorId_existente_devuelve200() throws Exception {
        when(carreraServicio.obtenerPorId(carreraId)).thenReturn(carreraResponseDTO);

        mockMvc.perform(get("/api/v1/carreras/{carreraId}", carreraId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carreraId").value(carreraId.toString()));
    }
    @Test
    void obtenerPorId_inexistente_devuelve404() throws Exception {
        when(carreraServicio.obtenerPorId(carreraId))
                .thenThrow(new CarreraNoEncontradaException("No se encontro la carrera con id " + carreraId));

        mockMvc.perform(get("/api/v1/carreras/{carreraId}", carreraId))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_datosValidos_devuelve200() throws Exception {
        CarreraRequestDTO request = new CarreraRequestDTO("Ingenieria Industrial");
        CarreraResponseDTO actualizada = new CarreraResponseDTO(carreraId, "Ingenieria Industrial", false);
        when(carreraServicio.actualizar(eq(carreraId), any(CarreraRequestDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/carreras/{carreraId}", carreraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ingenieria Industrial"));
    }

    @Test
    void darDeBaja_existente_devuelve204() throws Exception {
        doNothing().when(carreraServicio).darDeBaja(carreraId);

        mockMvc.perform(patch("/api/v1/carreras/{carreraId}/baja", carreraId))
                .andExpect(status().isNoContent());

        verify(carreraServicio).darDeBaja(carreraId);
    }

    @Test
    void reactivar_existente_devuelve204() throws Exception {
        doNothing().when(carreraServicio).reactivar(carreraId);

        mockMvc.perform(patch("/api/v1/carreras/{carreraId}/reactivar", carreraId))
                .andExpect(status().isNoContent());

        verify(carreraServicio).reactivar(carreraId);
    }
}
