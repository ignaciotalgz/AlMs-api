package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaRequestDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.DocenteAsignatura;
import com.algz.alms.enumeraciones.Cargo;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.excepciones.DocenteAsignaturaNoEncontradaException;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.repositorios.AsignaturaRepositorio;
import com.algz.alms.repositorios.DocenteAsignaturaRepositorio;
import com.algz.alms.repositorios.DocenteRepositorio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class DocenteAsignaturaServicioTest {
    @Mock
    private DocenteAsignaturaRepositorio docenteAsignaturaRepositorio;

    @Mock
    private DocenteRepositorio docenteRepositorio;

    @Mock
    private AsignaturaRepositorio asignaturaRepositorio;

    @Mock
    private Docente docente;

    private DocenteAsignaturaServicio docenteAsignaturaServicio;

    private UUID docenteAsignaturaId;
    private UUID docenteId;
    private UUID asignaturaId;
    private Asignatura asignatura;
    private DocenteAsignatura docenteAsignatura;

    @BeforeEach
    void setUp() {
        docenteAsignaturaServicio = new DocenteAsignaturaServicio(
                docenteAsignaturaRepositorio, docenteRepositorio, asignaturaRepositorio);

        docenteAsignaturaId = UUID.randomUUID();
        docenteId = UUID.randomUUID();
        asignaturaId = UUID.randomUUID();

        asignatura = Asignatura.builder()
                .asignaturaId(asignaturaId).nombre("Algebra I").codigo("MAT-101").baja(false).build();

        docenteAsignatura = DocenteAsignatura.builder()
                .docenteAsignaturaId(docenteAsignaturaId)
                .docente(docente)
                .asignatura(asignatura)
                .cargo(Cargo.TITULAR)
                .fechaDesde(LocalDate.of(2024, 3, 1))
                .baja(false)
                .build();
    }

    @Test
    void crear_docenteYAsignaturaExisten_creaAsignacion() {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteRepositorio.findById(docenteId)).thenReturn(Optional.of(docente));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));
        when(docenteAsignaturaRepositorio
                .existsByDocente_PersonaIdAndAsignatura_AsignaturaIdAndCargoAndBajaFalse(
                        docenteId, asignaturaId, Cargo.TITULAR))
                .thenReturn(false);
        when(docente.getPersonaId()).thenReturn(docenteId);
        when(docente.getLegajo()).thenReturn("LEG-001");

        var resultado = docenteAsignaturaServicio.crear(request);

        assertThat(resultado.cargo()).isEqualTo(Cargo.TITULAR);
        verify(docenteAsignaturaRepositorio).save(any(DocenteAsignatura.class));
    }

    @Test
    void crear_docenteInexistente_lanzaExcepcion() {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteRepositorio.findById(docenteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteAsignaturaServicio.crear(request))
                .isInstanceOf(DocenteNoEncontradoException.class);
    }

    @Test
    void crear_asignaturaInexistente_lanzaExcepcion() {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteRepositorio.findById(docenteId)).thenReturn(Optional.of(docente));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteAsignaturaServicio.crear(request))
                .isInstanceOf(AsignaturaNoEncontradaException.class);
    }

    @Test
    void crear_asignacionActivaDuplicada_lanzaExcepcion() {
        DocenteAsignaturaRequestDTO request = new DocenteAsignaturaRequestDTO(
                docenteId, asignaturaId, Cargo.TITULAR, LocalDate.of(2024, 3, 1), null);
        when(docenteRepositorio.findById(docenteId)).thenReturn(Optional.of(docente));
        when(asignaturaRepositorio.findById(asignaturaId)).thenReturn(Optional.of(asignatura));
        when(docenteAsignaturaRepositorio
                .existsByDocente_PersonaIdAndAsignatura_AsignaturaIdAndCargoAndBajaFalse(
                        docenteId, asignaturaId, Cargo.TITULAR))
                .thenReturn(true);

        assertThatThrownBy(() -> docenteAsignaturaServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asignacion activa");

        verify(docenteAsignaturaRepositorio, never()).save(any());
    }

    @Test
    void listarPorAsignatura_asignaturaInexistente_lanzaExcepcion() {
        when(asignaturaRepositorio.existsById(asignaturaId)).thenReturn(false);

        assertThatThrownBy(() -> docenteAsignaturaServicio.listarPorAsignatura(asignaturaId, false))
                .isInstanceOf(AsignaturaNoEncontradaException.class);
    }

    @Test
    void listarPorDocente_docenteInexistente_lanzaExcepcion() {
        when(docenteRepositorio.existsById(docenteId)).thenReturn(false);

        assertThatThrownBy(() -> docenteAsignaturaServicio.listarPorDocente(docenteId, false))
                .isInstanceOf(DocenteNoEncontradoException.class);
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(docenteAsignaturaRepositorio.findById(docenteAsignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> docenteAsignaturaServicio.obtenerPorId(docenteAsignaturaId))
                .isInstanceOf(DocenteAsignaturaNoEncontradaException.class);
    }

    @Test
    void darDeBaja_marcaBajaTrueYSeteaFechaBaja() {
        when(docenteAsignaturaRepositorio.findById(docenteAsignaturaId)).thenReturn(Optional.of(docenteAsignatura));

        docenteAsignaturaServicio.darDeBaja(docenteAsignaturaId);

        assertThat(docenteAsignatura.isBaja()).isTrue();
        assertThat(docenteAsignatura.getFechaBaja()).isEqualTo(LocalDate.now());
    }
}
