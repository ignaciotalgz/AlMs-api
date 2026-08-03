package com.algz.alms.controladores;

import java.time.LocalDate;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.algz.alms.dtos.docente.DocenteResponse;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.DocenteServicio;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DocenteControlador.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DocenteControlador")
class DocenteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocenteServicio docenteServicio;

    private UUID personaId;
    private DocenteResponse response;

    @BeforeEach
    void setUp() {
        personaId = UUID.randomUUID();
        response = new DocenteResponse(
            personaId, "37500488", "Alvarez Gonzalez", "Hector Jose", "hector@alms.com",
            "381331910", "Necochea 150", "DO-2026-0001", LocalDate.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/docentes retorna 200 y la lista de docentes")
    void listar_retorna200() throws Exception {
        when(docenteServicio.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/docentes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].legajo").value("DO-2026-0001"));
    }

    @Test
    @DisplayName("GET /api/v1/docentes/{id} retorna 200 cuando el docente existe")
    void obtener_retorna200CuandoExiste() throws Exception {
        when(docenteServicio.obtenerPorId(personaId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/docentes/{id}", personaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.legajo").value("DO-2026-0001"))
            .andExpect(jsonPath("$.documento").value("37500488"));
    }

    @Test
    @DisplayName("GET /api/v1/docentes/{id} retorna 404 cuando no existe")
    void obtener_retorna404CuandoNoExiste() throws Exception {
        when(docenteServicio.obtenerPorId(personaId)).thenThrow(new DocenteNoEncontradoException("Docente no encontrado con id: " + personaId));

        mockMvc.perform(get("/api/v1/docentes/{id}", personaId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}