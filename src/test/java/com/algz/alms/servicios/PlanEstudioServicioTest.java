package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.planestudio.PlanEstudioRequestDTO;
import com.algz.alms.dtos.planestudio.PlanEstudioResponseDTO;
import com.algz.alms.entidades.Carrera;
import com.algz.alms.entidades.PlanEstudio;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.repositorios.CarreraRepositorio;
import com.algz.alms.repositorios.PlanEstudioRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class PlanEstudioServicioTest {
    @Mock
    private PlanEstudioRepositorio planEstudioRepositorio;
    @Mock
    private CarreraRepositorio carreraRepositorio;
    private PlanEstudioServicio planEstudioServicio;

    private UUID planEstudioId;
    private UUID carreraId;
    private Carrera carrera;
    private PlanEstudio planEstudio;

    @BeforeEach
    void setUp() {
        planEstudioServicio = new PlanEstudioServicio(planEstudioRepositorio, carreraRepositorio);
        carreraId = UUID.randomUUID();
        planEstudioId = UUID.randomUUID();
        carrera = Carrera.builder().carreraId(carreraId).nombre("Ingenieria en Sistemas").baja(false).build();
        planEstudio = PlanEstudio.builder()
                .planEstudioId(planEstudioId)
                .carrera(carrera)
                .nombre("Plan 2024")
                .vigenciaDesde(LocalDate.of(2024, 3, 1))
                .baja(false)
                .build();
    }
    @Test
    void crear_carreraExisteYNombreLibre_creaPlan() {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2024", LocalDate.of(2024, 3, 1), null);
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));
        when(planEstudioRepositorio.existsByCarrera_CarreraIdAndNombreIgnoreCase(carreraId, "Plan 2024"))
                .thenReturn(false);

        PlanEstudioResponseDTO resultado = planEstudioServicio.crear(request);

        assertThat(resultado.nombre()).isEqualTo("Plan 2024");
        assertThat(resultado.carreraId()).isEqualTo(carreraId);
        verify(planEstudioRepositorio).save(any(PlanEstudio.class));
    }
    @Test
    void crear_carreraInexistente_lanzaExcepcion() {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2024", LocalDate.of(2024, 3, 1), null);
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planEstudioServicio.crear(request))
                .isInstanceOf(CarreraNoEncontradaException.class);

        verify(planEstudioRepositorio, never()).save(any());
    }
    @Test
    void crear_nombreDuplicadoEnMismaCarrera_lanzaExcepcion() {
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                carreraId, "Plan 2024", LocalDate.of(2024, 3, 1), null);
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));
        when(planEstudioRepositorio.existsByCarrera_CarreraIdAndNombreIgnoreCase(carreraId, "Plan 2024"))
                .thenReturn(true);

        assertThatThrownBy(() -> planEstudioServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
    }
    @Test
    void obtenerPorId_existente_devuelveDTO() {
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));

        PlanEstudioResponseDTO resultado = planEstudioServicio.obtenerPorId(planEstudioId);

        assertThat(resultado.planEstudioId()).isEqualTo(planEstudioId);
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planEstudioServicio.obtenerPorId(planEstudioId))
                .isInstanceOf(PlanEstudioNoEncontradoException.class);
    }
    @Test
    void listarPorCarrera_carreraInexistente_lanzaExcepcion() {
        when(carreraRepositorio.existsById(carreraId)).thenReturn(false);

        assertThatThrownBy(() -> planEstudioServicio.listarPorCarrera(carreraId, false))
                .isInstanceOf(CarreraNoEncontradaException.class);

        verify(planEstudioRepositorio, never()).findByCarrera_CarreraId(any());
    }
    @Test
    void listarPorCarrera_sinIncluirBajas_filtraLosDeBaja() {
        PlanEstudio planDeBaja = PlanEstudio.builder()
                .planEstudioId(UUID.randomUUID())
                .carrera(carrera)
                .nombre("Plan Viejo")
                .vigenciaDesde(LocalDate.of(2020, 1, 1))
                .baja(true)
                .build();
        when(carreraRepositorio.existsById(carreraId)).thenReturn(true);
        when(planEstudioRepositorio.findByCarrera_CarreraId(carreraId))
                .thenReturn(List.of(planEstudio, planDeBaja));

        List<PlanEstudioResponseDTO> resultado = planEstudioServicio.listarPorCarrera(carreraId, false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Plan 2024");
    }
    @Test
    void actualizar_existente_actualizaDatos() {
        UUID nuevaCarreraId = UUID.randomUUID();
        Carrera nuevaCarrera = Carrera.builder()
                .carreraId(nuevaCarreraId).nombre("Ing. Industrial").baja(false).build();
        PlanEstudioRequestDTO request = new PlanEstudioRequestDTO(
                nuevaCarreraId, "Plan 2025", LocalDate.of(2025, 3, 1), null);

        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));
        when(carreraRepositorio.findById(nuevaCarreraId)).thenReturn(Optional.of(nuevaCarrera));

        PlanEstudioResponseDTO resultado = planEstudioServicio.actualizar(planEstudioId, request);

        assertThat(resultado.nombre()).isEqualTo("Plan 2025");
        assertThat(resultado.carreraId()).isEqualTo(nuevaCarreraId);
    }
    @Test
    void darDeBaja_existente_marcaBajaTrue() {
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));

        planEstudioServicio.darDeBaja(planEstudioId);

        assertThat(planEstudio.isBaja()).isTrue();
    }

    @Test
    void reactivar_existente_marcaBajaFalse() {
        planEstudio.setBaja(true);
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));

        planEstudioServicio.reactivar(planEstudioId);

        assertThat(planEstudio.isBaja()).isFalse();
    }
}
