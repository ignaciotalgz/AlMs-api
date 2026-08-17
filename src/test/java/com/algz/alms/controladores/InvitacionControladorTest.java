package com.algz.alms.controladores;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.algz.alms.dtos.invitacion.InvitacionRequest;
import com.algz.alms.dtos.invitacion.InvitacionResponse;
import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.enumeraciones.TipoInvitacion;
import com.algz.alms.excepciones.InvitacionInvalidaException;
import com.algz.alms.excepciones.InvitacionNoEncontradaException;
import com.algz.alms.filtros.JwtAuthenticationFilter;
import com.algz.alms.servicios.InvitacionServicio;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(
    controllers = InvitacionControlador.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
//@org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InvitacionControladorTest")
public class InvitacionControladorTest {
    @Autowired
    private MockMvc mockMvc;
    // @Autowired
    // private ObjectMapper objectMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private InvitacionServicio invitacionServicio;

    private RequestPostProcessor comoAdmin;

    @BeforeEach
    void setUp() {
        Usuario admin = Usuario.builder().email("admin@alms.com").rol(Rol.ROLE_ADMIN).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
        comoAdmin = authentication(auth);
    }

    @Test
    @DisplayName("POST /api/v1/invitaciones retorna 201 con el token generado")
    void generar_retorna201() throws Exception {
        InvitacionRequest request = new InvitacionRequest(TipoInvitacion.ALUMNO);
        InvitacionResponse response = new InvitacionResponse("token-crudo-abc", TipoInvitacion.ALUMNO, LocalDateTime.now().plusHours(24));
        when(invitacionServicio.generar(org.mockito.ArgumentMatchers.eq(TipoInvitacion.ALUMNO), org.mockito.ArgumentMatchers.any()))
            .thenReturn(response);

        mockMvc.perform(post("/api/v1/invitaciones")
                .with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("token-crudo-abc"))
            .andExpect(jsonPath("$.tipo").value("ALUMNO"));
    }

    @Test
    @DisplayName("POST /api/v1/invitaciones retorna 400 si el tipo falta")
    void generar_retorna400CuandoFaltaTipo() throws Exception {
        mockMvc.perform(post("/api/v1/invitaciones")
                .with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/invitaciones/{id}/revocar retorna 204")
    void revocar_retorna204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(invitacionServicio).revocar(id);

        mockMvc.perform(patch("/api/v1/invitaciones/{id}/revocar", id).with(comoAdmin))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/v1/invitaciones/{id}/revocar retorna 404 si no existe")
    void revocar_retorna404SiNoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InvitacionNoEncontradaException("Invitación no encontrada con id: " + id))
            .when(invitacionServicio).revocar(id);

        mockMvc.perform(patch("/api/v1/invitaciones/{id}/revocar", id).with(comoAdmin))
            .andExpect(status().isNotFound());
    }
    @Test
    @DisplayName("PATCH /api/v1/invitaciones/{id}/revocar retorna 400 si ya estaba usada")
    void revocar_retorna400SiYaUsada() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InvitacionInvalidaException("La invitación ya fue utilizada o revocada"))
            .when(invitacionServicio).revocar(id);

        mockMvc.perform(patch("/api/v1/invitaciones/{id}/revocar", id).with(comoAdmin))
            .andExpect(status().isBadRequest());
    }
}
