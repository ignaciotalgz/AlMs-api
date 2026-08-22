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

import com.algz.alms.dtos.modulo.ModuloRequestDTO;
import com.algz.alms.dtos.modulo.ModuloResponseDTO;
import com.algz.alms.entidades.Modulo;
import com.algz.alms.entidades.PlanEstudio;
import com.algz.alms.excepciones.ModuloNoEncontradoException;
import com.algz.alms.excepciones.PlanEstudioNoEncontradoException;
import com.algz.alms.repositorios.ModuloRepositorio;
import com.algz.alms.repositorios.PlanEstudioRepositorio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ModuloServicioTest {
@Mock
    private ModuloRepositorio moduloRepositorio;

    @Mock
    private PlanEstudioRepositorio planEstudioRepositorio;

    private ModuloServicio moduloServicio;

    private UUID moduloId;
    private UUID planEstudioId;
    private PlanEstudio planEstudio;
    private Modulo modulo;

    @BeforeEach
    void setUp() {
        moduloServicio = new ModuloServicio(moduloRepositorio, planEstudioRepositorio);
        moduloId = UUID.randomUUID();
        planEstudioId = UUID.randomUUID();
        planEstudio = PlanEstudio.builder().planEstudioId(planEstudioId).nombre("Plan 2024").build();
        modulo = Modulo.builder()
                .moduloId(moduloId).planEstudio(planEstudio).nombre("Ciclo Basico").orden(1).baja(false).build();
    }

    @Test
    void crear_planExisteYOrdenLibre_creaModulo() {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 1);
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));
        when(moduloRepositorio.existsByPlanEstudio_PlanEstudioIdAndOrden(planEstudioId, 1)).thenReturn(false);

        ModuloResponseDTO resultado = moduloServicio.crear(request);

        assertThat(resultado.orden()).isEqualTo(1);
        verify(moduloRepositorio).save(any(Modulo.class));
    }

    @Test
    void crear_planInexistente_lanzaExcepcion() {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 1);
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloServicio.crear(request))
                .isInstanceOf(PlanEstudioNoEncontradoException.class);

        verify(moduloRepositorio, never()).save(any());
    }

    @Test
    void crear_ordenDuplicadoEnMismoPlan_lanzaExcepcion() {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Basico", 1);
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));
        when(moduloRepositorio.existsByPlanEstudio_PlanEstudioIdAndOrden(planEstudioId, 1)).thenReturn(true);

        assertThatThrownBy(() -> moduloServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orden");
    }

    @Test
    void listarPorPlan_planInexistente_lanzaExcepcion() {
        when(planEstudioRepositorio.existsById(planEstudioId)).thenReturn(false);

        assertThatThrownBy(() -> moduloServicio.listarPorPlan(planEstudioId, false))
                .isInstanceOf(PlanEstudioNoEncontradoException.class);
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moduloServicio.obtenerPorId(moduloId))
                .isInstanceOf(ModuloNoEncontradoException.class);
    }

    @Test
    void actualizar_ordenLibreEnMismoModulo_actualiza() {
        ModuloRequestDTO request = new ModuloRequestDTO(planEstudioId, "Ciclo Nuevo", 2);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));
        when(planEstudioRepositorio.findById(planEstudioId)).thenReturn(Optional.of(planEstudio));
        when(moduloRepositorio.existsByPlanEstudio_PlanEstudioIdAndOrdenAndModuloIdNot(planEstudioId, 2, moduloId))
                .thenReturn(false);

        ModuloResponseDTO resultado = moduloServicio.actualizar(moduloId, request);

        assertThat(resultado.nombre()).isEqualTo("Ciclo Nuevo");
        assertThat(resultado.orden()).isEqualTo(2);
    }

    @Test
    void darDeBaja_marcaBajaTrue() {
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));

        moduloServicio.darDeBaja(moduloId);

        assertThat(modulo.isBaja()).isTrue();
    }

    @Test
    void reactivar_marcaBajaFalse() {
        modulo.setBaja(true);
        when(moduloRepositorio.findById(moduloId)).thenReturn(Optional.of(modulo));

        moduloServicio.reactivar(moduloId);

        assertThat(modulo.isBaja()).isFalse();
    }
}
