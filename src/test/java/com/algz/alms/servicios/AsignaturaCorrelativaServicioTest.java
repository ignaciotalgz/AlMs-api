package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.asignaturacorrelativa.AsignaturaCorrelativaRequestDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.entidades.AsignaturaCorrelativa;
import com.algz.alms.entidades.Modulo;
import com.algz.alms.entidades.ModuloAsignatura;
import com.algz.alms.enumeraciones.TipoCorrelatividad;
import com.algz.alms.excepciones.AsignaturaCorrelativaNoEncontradaException;
import com.algz.alms.excepciones.ModuloAsignaturaNoEncontradaException;
import com.algz.alms.repositorios.AsignaturaCorrelativaRepositorio;
import com.algz.alms.repositorios.ModuloAsignaturaRepositorio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class AsignaturaCorrelativaServicioTest {
    @Mock
    private AsignaturaCorrelativaRepositorio asignaturaCorrelativaRepositorio;

    @Mock
    private ModuloAsignaturaRepositorio moduloAsignaturaRepositorio;

    private AsignaturaCorrelativaServicio asignaturaCorrelativaServicio;

    private UUID correlativaId;
    private UUID moduloAsignaturaId;
    private UUID moduloAsignaturaRequeridaId;
    private ModuloAsignatura moduloAsignatura;
    private ModuloAsignatura moduloAsignaturaRequerida;
    private AsignaturaCorrelativa correlativa;

    @BeforeEach
    void setUp() {
        asignaturaCorrelativaServicio = new AsignaturaCorrelativaServicio(
                asignaturaCorrelativaRepositorio, moduloAsignaturaRepositorio);

        moduloAsignaturaId = UUID.randomUUID();
        moduloAsignaturaRequeridaId = UUID.randomUUID();
        correlativaId = UUID.randomUUID();

        Modulo modulo = Modulo.builder().moduloId(UUID.randomUUID()).nombre("Modulo A").orden(1).baja(false).build();
        Asignatura analisis = Asignatura.builder()
                .asignaturaId(UUID.randomUUID()).nombre("Analisis II").codigo("MAT-201").baja(false).build();
        Asignatura algebra = Asignatura.builder()
                .asignaturaId(UUID.randomUUID()).nombre("Algebra I").codigo("MAT-101").baja(false).build();

        moduloAsignatura = ModuloAsignatura.builder()
                .moduloAsignaturaId(moduloAsignaturaId).modulo(modulo).asignatura(analisis).orden(2).baja(false)
                .build();
        moduloAsignaturaRequerida = ModuloAsignatura.builder()
                .moduloAsignaturaId(moduloAsignaturaRequeridaId).modulo(modulo).asignatura(algebra).orden(1)
                .baja(false).build();

        correlativa = AsignaturaCorrelativa.builder()
                .asignaturaCorrelativaId(correlativaId)
                .moduloAsignatura(moduloAsignatura)
                .moduloAsignaturaRequerida(moduloAsignaturaRequerida)
                .tipoCorrelatividad(TipoCorrelatividad.CURSAR_REGULARIZADA)
                .baja(false)
                .build();
    }

    @Test
    void crear_ambasExisten_creaCorrelativa() {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_REGULARIZADA);
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.of(moduloAsignatura));
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaRequeridaId))
                .thenReturn(Optional.of(moduloAsignaturaRequerida));
        when(asignaturaCorrelativaRepositorio
                .existsByModuloAsignatura_ModuloAsignaturaIdAndModuloAsignaturaRequerida_ModuloAsignaturaIdAndTipoCorrelatividad(
                        moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_REGULARIZADA))
                .thenReturn(false);

        var resultado = asignaturaCorrelativaServicio.crear(request);

        assertThat(resultado.tipoCorrelatividad()).isEqualTo(TipoCorrelatividad.CURSAR_REGULARIZADA);
        verify(asignaturaCorrelativaRepositorio).save(any(AsignaturaCorrelativa.class));
    }

    @Test
    void crear_autoreferencia_lanzaExcepcion() {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaId, TipoCorrelatividad.CURSAR_APROBADA);

        assertThatThrownBy(() -> asignaturaCorrelativaServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("si misma");

        verifyNoInteractions(moduloAsignaturaRepositorio, asignaturaCorrelativaRepositorio);
    }

    @Test
    void crear_moduloAsignaturaInexistente_lanzaExcepcion() {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_APROBADA);
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignaturaCorrelativaServicio.crear(request))
                .isInstanceOf(ModuloAsignaturaNoEncontradaException.class);
    }

    @Test
    void crear_correlativaDuplicada_lanzaExcepcion() {
        AsignaturaCorrelativaRequestDTO request = new AsignaturaCorrelativaRequestDTO(
                moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_REGULARIZADA);
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaId)).thenReturn(Optional.of(moduloAsignatura));
        when(moduloAsignaturaRepositorio.findById(moduloAsignaturaRequeridaId))
                .thenReturn(Optional.of(moduloAsignaturaRequerida));
        when(asignaturaCorrelativaRepositorio
                .existsByModuloAsignatura_ModuloAsignaturaIdAndModuloAsignaturaRequerida_ModuloAsignaturaIdAndTipoCorrelatividad(
                        moduloAsignaturaId, moduloAsignaturaRequeridaId, TipoCorrelatividad.CURSAR_REGULARIZADA))
                .thenReturn(true);

        assertThatThrownBy(() -> asignaturaCorrelativaServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya esta registrada");
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(asignaturaCorrelativaRepositorio.findById(correlativaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asignaturaCorrelativaServicio.obtenerPorId(correlativaId))
                .isInstanceOf(AsignaturaCorrelativaNoEncontradaException.class);
    }

    @Test
    void darDeBaja_marcaBajaTrue() {
        when(asignaturaCorrelativaRepositorio.findById(correlativaId)).thenReturn(Optional.of(correlativa));

        asignaturaCorrelativaServicio.darDeBaja(correlativaId);

        assertThat(correlativa.isBaja()).isTrue();
    }

    @Test
    void reactivar_marcaBajaFalse() {
        correlativa.setBaja(true);
        when(asignaturaCorrelativaRepositorio.findById(correlativaId)).thenReturn(Optional.of(correlativa));

        asignaturaCorrelativaServicio.reactivar(correlativaId);

        assertThat(correlativa.isBaja()).isFalse();
    }
}
