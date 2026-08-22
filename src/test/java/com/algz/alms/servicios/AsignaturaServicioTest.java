package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.asignatura.AsignaturaRequestDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.repositorios.AsignaturaRepositorio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class AsignaturaServicioTest {
    @Mock
    private AsignaturaRepositorio asignaturaRepositorio;

    private AsignaturaServicio asignaturaServicio;

    private UUID asignaturaId;
    private Asignatura asignatura;

    @BeforeEach
    void setUp() {
        asignaturaServicio = new AsignaturaServicio(asignaturaRepositorio);
        asignaturaId = UUID.randomUUID();
        asignatura = Asignatura.builder()
                .asignaturaId(asignaturaId).nombre("Algebra I").codigo("MAT-101").baja(false).build();
    }

    @Test
    void crear_codigoLibre_creaAsignatura() {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I", "MAT-101");
        when(asignaturaRepositorio.existsByCodigoIgnoreCase("MAT-101")).thenReturn(false);

        var resultado = asignaturaServicio.crear(request);

        assertThat(resultado.codigo()).isEqualTo("MAT-101");
        verify(asignaturaRepositorio).save(any(Asignatura.class));
    }

    @Test
    void crear_codigoDuplicado_lanzaExcepcion() {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I", "MAT-101");
        when(asignaturaRepositorio.existsByCodigoIgnoreCase("MAT-101")).thenReturn(true);

        assertThatThrownBy(() -> asignaturaServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(asignaturaRepositorio, never()).save(any());
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignaturaServicio.obtenerPorId(asignaturaId))
                .isInstanceOf(AsignaturaNoEncontradaException.class);
    }

    @Test
    void actualizar_codigoLibre_actualiza() {
        AsignaturaRequestDTO request = new AsignaturaRequestDTO("Algebra I Renovada", "MAT-102");
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));
        when(asignaturaRepositorio.existsByCodigoIgnoreCaseAndAsignaturaIdNot("MAT-102", asignaturaId))
                .thenReturn(false);

        var resultado = asignaturaServicio.actualizar(asignaturaId, request);

        assertThat(resultado.codigo()).isEqualTo("MAT-102");
    }

    @Test
    void darDeBaja_marcaBajaTrue() {
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));

        asignaturaServicio.darDeBaja(asignaturaId);

        assertThat(asignatura.isBaja()).isTrue();
    }

    @Test
    void reactivar_marcaBajaFalse() {
        asignatura.setBaja(true);
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));

        asignaturaServicio.reactivar(asignaturaId);

        assertThat(asignatura.isBaja()).isFalse();
    }
}
