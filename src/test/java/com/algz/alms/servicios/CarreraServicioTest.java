package com.algz.alms.servicios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algz.alms.dtos.carrera.CarreraRequestDTO;
import com.algz.alms.dtos.carrera.CarreraResponseDTO;
import com.algz.alms.entidades.Carrera;
import com.algz.alms.excepciones.CarreraNoEncontradaException;
import com.algz.alms.repositorios.CarreraRepositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class CarreraServicioTest {
    @Mock
    private CarreraRepositorio carreraRepositorio;
    private CarreraServicio carreraServicio;

    private UUID carreraId;
    private Carrera carrera;

    @BeforeEach
    void setUp() {
        carreraServicio = new CarreraServicio(carreraRepositorio);
        carreraId = UUID.randomUUID();
        carrera = Carrera.builder()
                .carreraId(carreraId)
                .nombre("Ingenieria en Sistemas")
                .baja(false)
                .build();
    }
    @Test
    void crear_nombreNoExiste_creaCarrera() {
        CarreraRequestDTO request = new CarreraRequestDTO("Ingenieria en Sistemas");
        when(carreraRepositorio.existsByNombreIgnoreCase("Ingenieria en Sistemas")).thenReturn(false);

        CarreraResponseDTO resultado = carreraServicio.crear(request);

        assertThat(resultado.nombre()).isEqualTo("Ingenieria en Sistemas");
        assertThat(resultado.baja()).isFalse();
        verify(carreraRepositorio).save(any(Carrera.class));
    }

    @Test
    void crear_nombreYaExiste_lanzaExcepcion() {
        CarreraRequestDTO request = new CarreraRequestDTO("Ingenieria en Sistemas");
        when(carreraRepositorio.existsByNombreIgnoreCase("Ingenieria en Sistemas")).thenReturn(true);

        assertThatThrownBy(() -> carreraServicio.crear(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");

        verify(carreraRepositorio, never()).save(any());
    }
    @Test
    void obtenerPorId_existente_devuelveDTO() {
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));

        CarreraResponseDTO resultado = carreraServicio.obtenerPorId(carreraId);

        assertThat(resultado.carreraId()).isEqualTo(carreraId);
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carreraServicio.obtenerPorId(carreraId))
                .isInstanceOf(CarreraNoEncontradaException.class);
    }
    @Test
    void listarTodas_sinIncluirBajas_filtraLasDeBaja() {
        Carrera carreraDeBaja = Carrera.builder()
                .carreraId(UUID.randomUUID())
                .nombre("Carrera Vieja")
                .baja(true)
                .build();
        when(carreraRepositorio.findAll()).thenReturn(List.of(carrera, carreraDeBaja));

        List<CarreraResponseDTO> resultado = carreraServicio.listarTodas(false);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Ingenieria en Sistemas");
    }
    @Test
    void listarTodas_incluirBajas_devuelveTodas() {
        Carrera carreraDeBaja = Carrera.builder()
                .carreraId(UUID.randomUUID())
                .nombre("Carrera Vieja")
                .baja(true)
                .build();
        when(carreraRepositorio.findAll()).thenReturn(List.of(carrera, carreraDeBaja));

        List<CarreraResponseDTO> resultado = carreraServicio.listarTodas(true);

        assertThat(resultado).hasSize(2);
    }
    @Test
    void darDeBaja_existente_marcaBajaTrue() {
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));

        carreraServicio.darDeBaja(carreraId);

        assertThat(carrera.isBaja()).isTrue();
    }

    @Test
    void reactivar_existente_marcaBajaFalse() {
        carrera.setBaja(true);
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));

        carreraServicio.reactivar(carreraId);

        assertThat(carrera.isBaja()).isFalse();
    }
    @Test
    void actualizar_existente_actualizaNombre() {
        CarreraRequestDTO request = new CarreraRequestDTO("Nuevo Nombre");
        when(carreraRepositorio.findById(carreraId)).thenReturn(Optional.of(carrera));

        CarreraResponseDTO resultado = carreraServicio.actualizar(carreraId, request);

        assertThat(resultado.nombre()).isEqualTo("Nuevo Nombre");
    }
}
