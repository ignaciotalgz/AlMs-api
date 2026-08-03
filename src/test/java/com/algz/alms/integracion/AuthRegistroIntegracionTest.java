package com.algz.alms.integracion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.algz.alms.dtos.alumno.AlumnoRegistroRequest;
import com.algz.alms.dtos.docente.DocenteRegistroRequest;
import com.algz.alms.dtos.invitacion.InvitacionRequest;
import com.algz.alms.dtos.usuario.LoginRequest;
import com.algz.alms.enumeraciones.Rol;
import com.algz.alms.enumeraciones.TipoInvitacion;
import com.algz.alms.repositorios.AlumnoRepositorio;
import com.algz.alms.repositorios.DocenteRepositorio;
import com.algz.alms.repositorios.InvitacionRegistroRepositorio;
import com.algz.alms.repositorios.PersonaRepositorio;
import com.algz.alms.repositorios.UsuarioRepositorio;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test de integración completo con seguridad real: admin genera invitación -> el token
// se usa una sola vez para completar Persona+Usuario+Alumno/Docente -> el usuario
// resultante puede loguearse. También cubre autorización real (401/403) sobre
// /api/v1/invitaciones, algo que el slice test de InvitacionControlador no puede probar
// porque desactiva los filtros de seguridad.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("Invitaciones + registro por rol (integración)")
class AuthRegistroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // @Autowired
    // private ObjectMapper objectMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private AlumnoRepositorio alumnoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PersonaRepositorio personaRepositorio;

    @Autowired
    private InvitacionRegistroRepositorio invitacionRegistroRepositorio;

    // Credenciales del admin sembrado por AdminSeeder, ver src/test/resources/application.properties
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_PASSWORD = "ClaveDeTest123";

    @BeforeEach
    void limpiarBaseDeDatos() {
        alumnoRepositorio.deleteAll();
        docenteRepositorio.deleteAll();
        invitacionRegistroRepositorio.deleteAll();
        // OJO: no usar usuarioRepositorio.deleteAll() acá. AdminSeeder solo corre una vez
        // al levantar el ApplicationContext (que @SpringBootTest cachea entre tests), así
        // que borrar todos los Usuario también borraría al admin y no se volvería a sembrar.
        usuarioRepositorio.findAll().stream()
            .filter(u -> u.getRol() != Rol.ROLE_ADMIN)
            .forEach(usuarioRepositorio::delete);
        personaRepositorio.deleteAll();
    }

    private String obtenerTokenAdmin() throws Exception {
        LoginRequest login = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult resultado = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    private String generarInvitacion(String tokenAdmin, TipoInvitacion tipo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/invitaciones")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InvitacionRequest(tipo))))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("Flujo completo: admin genera invitación de alumno -> registro exitoso -> login exitoso")
    void flujoCompletoAlumno_funciona() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();
        String tokenInvitacion = generarInvitacion(tokenAdmin, TipoInvitacion.ALUMNO);

        AlumnoRegistroRequest registro = new AlumnoRegistroRequest(
            tokenInvitacion, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/alumno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.legajo").value(org.hamcrest.Matchers.startsWith("AL-")));

        assertThat(alumnoRepositorio.findActivos()).hasSize(1);

        LoginRequest login = new LoginRequest("ignacio@alms.com", "ClaveSegura123");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rol").value("ROLE_ALUMNO"));
    }

    @Test
    @DisplayName("Flujo completo: admin genera invitación de docente -> registro exitoso")
    void flujoCompletoDocente_funciona() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();
        String tokenInvitacion = generarInvitacion(tokenAdmin, TipoInvitacion.DOCENTE);

        DocenteRegistroRequest registro = new DocenteRegistroRequest(
            tokenInvitacion, "37500488", "Alvarez Gonzalez", "Hector Jose", "hector@alms.com",
            "381331910", "Necochea 150", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/docente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.legajo").value(org.hamcrest.Matchers.startsWith("DO-")));

        assertThat(docenteRepositorio.findActivos()).hasSize(1);
    }

    @Test
    @DisplayName("El token de invitación no puede reutilizarse una segunda vez")
    void tokenInvitacion_noSePuedeReutilizar() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();
        String tokenInvitacion = generarInvitacion(tokenAdmin, TipoInvitacion.ALUMNO);

        AlumnoRegistroRequest primero = new AlumnoRegistroRequest(
            tokenInvitacion, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/alumno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(primero)))
            .andExpect(status().isCreated());

        AlumnoRegistroRequest segundo = new AlumnoRegistroRequest(
            tokenInvitacion, "11111111", "Otro Apellido", "Otro Nombre", "otro@alms.com",
            "3810000000", "Otra direccion", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/alumno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundo)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Un token de invitación de ALUMNO no sirve para registrarse como DOCENTE")
    void tokenDeAlumno_noSirveParaRegistroDocente() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();
        String tokenInvitacion = generarInvitacion(tokenAdmin, TipoInvitacion.ALUMNO);

        DocenteRegistroRequest registro = new DocenteRegistroRequest(
            tokenInvitacion, "37500488", "Alvarez Gonzalez", "Hector Jose", "hector@alms.com",
            "381331910", "Necochea 150", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/docente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Un token revocado por el admin ya no sirve para registrarse")
    void tokenRevocado_yaNoSirve() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();

        MvcResult creado = mockMvc.perform(post("/api/v1/invitaciones")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InvitacionRequest(TipoInvitacion.ALUMNO))))
            .andExpect(status().isCreated())
            .andReturn();
        var json = objectMapper.readTree(creado.getResponse().getContentAsString());
        String tokenInvitacion = json.get("token").asText();

        String invitacionId = invitacionRegistroRepositorio.findAll().get(0).getInvitacionId().toString();
        mockMvc.perform(patch("/api/v1/invitaciones/{id}/revocar", invitacionId)
                .header("Authorization", "Bearer " + tokenAdmin))
            .andExpect(status().isNoContent());

        AlumnoRegistroRequest registro = new AlumnoRegistroRequest(
            tokenInvitacion, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/alumno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/invitaciones sin autenticar retorna 401")
    void generarInvitacion_sinAutenticar_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/invitaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InvitacionRequest(TipoInvitacion.ALUMNO))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/invitaciones autenticado como ALUMNO (no admin) retorna 403")
    void generarInvitacion_comoAlumno_retorna403() throws Exception {
        String tokenAdmin = obtenerTokenAdmin();
        String tokenInvitacionAlumno = generarInvitacion(tokenAdmin, TipoInvitacion.ALUMNO);

        AlumnoRegistroRequest registro = new AlumnoRegistroRequest(
            tokenInvitacionAlumno, "39698370", "Alvarez Gonzalez", "Ignacio Tomás", "ignacio@alms.com",
            "3813527690", "Don bosco 2579", "ClaveSegura123"
        );
        mockMvc.perform(post("/api/v1/auth/registro/alumno")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registro)))
            .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("ignacio@alms.com", "ClaveSegura123");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andReturn();
        String tokenAlumno = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(post("/api/v1/invitaciones")
                .header("Authorization", "Bearer " + tokenAlumno)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InvitacionRequest(TipoInvitacion.DOCENTE))))
            .andExpect(status().isForbidden());
    }
}