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

import com.algz.alms.dtos.alumno.AlumnoResponse;
import com.algz.alms.excepciones.AlumnoNoEncontradoException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.AlumnoServicio;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// addFilters = false: slice del controlador, sin filtros de seguridad. Este controlador
// no tiene @PreAuthorize (cualquier usuario autenticado puede listar/ver), así que no
// hace falta inyectar un principal a mano como en InvitacionControladorTest.
@WebMvcTest(
    controllers = AlumnoControlador.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AlumnoControlador")
class AlumnoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlumnoServicio alumnoServicio;

    private UUID personaId;
    private AlumnoResponse response;

    @BeforeEach
    void setUp() {
        personaId = UUID.randomUUID();
        response = new AlumnoResponse(
            personaId, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "AL-2026-0001", LocalDate.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/alumnos retorna 200 y la lista de alumnos")
    void listar_retorna200() throws Exception {
        when(alumnoServicio.listar()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/alumnos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].legajo").value("AL-2026-0001"));
    }

    @Test
    @DisplayName("GET /api/v1/alumnos/{id} retorna 200 cuando el alumno existe")
    void obtener_retorna200CuandoExiste() throws Exception {
        when(alumnoServicio.obtenerPorId(personaId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/alumnos/{id}", personaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.legajo").value("AL-2026-0001"))
            .andExpect(jsonPath("$.documento").value("39698370"));
    }

    @Test
    @DisplayName("GET /api/v1/alumnos/{id} retorna 404 cuando no existe")
    void obtener_retorna404CuandoNoExiste() throws Exception {
        when(alumnoServicio.obtenerPorId(personaId)).thenThrow(new AlumnoNoEncontradoException("Alumno no encontrado con id: " + personaId));

        mockMvc.perform(get("/api/v1/alumnos/{id}", personaId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}