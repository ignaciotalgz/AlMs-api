package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.moduloasignatura.ModuloAsignaturaRequestDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.entidades.Modulo;
import com.algz.alms.entidades.ModuloAsignatura;
import com.algz.alms.entidades.PlanEstudio;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.excepciones.ModuloAsignaturaNoEncontradaException;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.repositorios.AsignaturaRepositorio;
import com.algz.alms.repositorios.ModuloAsignaturaRepositorio;
import com.algz.alms.repositorios.ModuloRepositorio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ModuloAsignaturaServicioTest {
    @Mock
    private ModuloAsignaturaRepositorio moduloAsignaturaRepositorio;

    @Mock
    private ModuloRepositorio moduloRepositorio;

    @Mock
    private AsignaturaRepositorio asignaturaRepositorio;

    private ModuloAsignaturaServicio moduloAsignaturaServicio;

    private UUID moduloAsignaturaId;
    private UUID moduloId;
    private UUID asignaturaId;
    private Modulo modulo;
    private Asignatura asignatura;
    private ModuloAsignatura moduloAsignatura;

    @BeforeEach
    void setUp() {
        moduloAsignaturaServicio = new ModuloAsignaturaServicio(
                moduloAsignaturaRepositorio, moduloRepositorio, asignaturaRepositorio);
        moduloAsignaturaId = UUID.randomUUID();
        moduloId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();

        PlanEstudio planEstudio = PlanEstudio.builder()
                .planEstudioId(UUID.randomUUID()).nombre("Plan 2024").build();
        modulo = Modulo.builder()
                .moduloId(moduloId).planEstudio(planEstudio).nombre("Ciclo Basico").orden(1).baja(false).build();
        asignatura = Asignatura.builder()
                .asignaturaId(asignaturaId).nombre("Algebra I").codigo("MAT-101").baja(false).build();
        moduloAsignatura = ModuloAsignatura.builder()
                .moduloAsignaturaId(moduloAsignaturaId).modulo(modulo).asignatura(asignatura).orden(1).baja(false)
                .build();
    }

    @Test
    void crear_moduloYAsignaturaExisten_creaRelacion() {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));
        when(moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndAsignatura_AsignaturaId(moduloId, asignaturaId))
                .thenReturn(false);
        when(moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndOrden(moduloId, 1)).thenReturn(false);

        var resultado = moduloAsignaturaServicio.crear(request);

        assertThat(resultado.asignaturaCodigo()).isEqualTo("MAT-101");
        verify(moduloAsignaturaRepositorio).save(any(ModuloAsignatura.class));
    }

    @Test
    void crear_moduloInexistente_lanzaExcepcion() {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloAsignaturaServicio.crear(request))
                .isInstanceOf(ModuloNoEncontradoException.class);
    }

    @Test
    void crear_asignaturaInexistente_lanzaExcepcion() {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloAsignaturaServicio.crear(request))
                .isInstanceOf(AsignaturaNoEncontradaException.class);
    }

    @Test
    void crear_asignaturaYaEnModulo_lanzaExcepcion() {
        ModuloAsignaturaRequestDTO request = new ModuloAsignaturaRequestDTO(moduloId, asignaturaId, 1);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));
        when(moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndAsignatura_AsignaturaId(moduloId, asignaturaId))
                .thenReturn(true);

        assertThatThrownBy(() -> moduloAsignaturaServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya esta agregada");
    }

    @Test
    void actualizarOrden_ordenLibre_actualiza() {
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.of(moduloAsignatura));
        when(moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndOrdenAndModuloAsignaturaIdNot(
                moduloId, 5, moduloAsignaturaId)).thenReturn(false);

        var resultado = moduloAsignaturaServicio.actualizarOrden(moduloAsignaturaId, 5);

        assertThat(resultado.orden()).isEqualTo(5);
    }

    @Test
    void actualizarOrden_ordenOcupado_lanzaExcepcion() {
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.of(moduloAsignatura));
        when(moduloAsignaturaRepositorio.existsByModulo_ModuloIdAndOrdenAndModuloAsignaturaIdNot(
                moduloId, 5, moduloAsignaturaId)).thenReturn(true);

        assertThatThrownBy(() -> moduloAsignaturaServicio.actualizarOrden(moduloAsignaturaId, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloAsignaturaServicio.obtenerPorId(moduloAsignaturaId))
                .isInstanceOf(ModuloAsignaturaNoEncontradaException.class);
    }

    @Test
    void darDeBaja_marcaBajaTrue() {
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.of(moduloAsignatura));

        moduloAsignaturaServicio.darDeBaja(moduloAsignaturaId);

        assertThat(moduloAsignatura.isBaja()).isTrue();
    }
}
