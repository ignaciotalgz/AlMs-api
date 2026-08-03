package com.algz.alms.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.algz.alms.dtos.alumno.AlumnoRegistroRequest;
import com.algz.alms.dtos.alumno.AlumnoResponse;
import com.algz.alms.dtos.docente.DocenteRegistroRequest;
import com.algz.alms.dtos.docente.DocenteResponse;
import com.algz.alms.dtos.usuario.AuthResponse;
import com.algz.alms.dtos.usuario.LoginRequest;
import com.algz.alms.servicios.AlumnoServicio;
import com.algz.alms.servicios.AuthServicio;
import com.algz.alms.servicios.DocenteServicio;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControlador {
    private final AuthServicio authServicio;
    private final AlumnoServicio alumnoServicio;
    private final DocenteServicio docenteServicio;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authServicio.login(request));
    }

    @PostMapping("/registro/alumno")
    public ResponseEntity<AlumnoResponse> registroAlumno(@Valid @RequestBody AlumnoRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alumnoServicio.registrar(request));
    }

    @PostMapping("/registro/docente")
    public ResponseEntity<DocenteResponse> registroDocente(@Valid @RequestBody DocenteRegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docenteServicio.registrar(request));
    }
}
