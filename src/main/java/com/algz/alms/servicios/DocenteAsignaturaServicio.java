package com.algz.alms.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaRequestDTO;
import com.algz.alms.dtos.docenteasignatura.DocenteAsignaturaResponseDTO;
import com.algz.alms.entidades.Asignatura;
import com.algz.alms.entidades.Docente;
import com.algz.alms.entidades.DocenteAsignatura;
import com.algz.alms.excepciones.AsignaturaNoEncontradaException;
import com.algz.alms.excepciones.DocenteAsignaturaNoEncontradaException;
import com.algz.alms.excepciones.DocenteNoEncontradoException;
import com.algz.alms.repositorios.AsignaturaRepositorio;
import com.algz.alms.repositorios.DocenteAsignaturaRepositorio;
import com.algz.alms.repositorios.DocenteRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocenteAsignaturaServicio {
    private final DocenteAsignaturaRepositorio docenteAsignaturaRepositorio;
    private final DocenteRepositorio docenteRepositorio;
    private final AsignaturaRepositorio asignaturaRepositorio;

    @Transactional
    public DocenteAsignaturaResponseDTO crear(DocenteAsignaturaRequestDTO request) {
        Docente docente = docenteRepositorio.findById(request.docenteId())
                .orElseThrow(() -> new DocenteNoEncontradoException(
                        "No se encontro el docente con id " + request.docenteId()));

        Asignatura asignatura = asignaturaRepositorio.findById(request.asignaturaId())
                .orElseThrow(() -> new AsignaturaNoEncontradaException(
                        "No se encontro la asignatura con id " + request.asignaturaId()));

        if (docenteAsignaturaRepositorio
                .existsByDocente_PersonaIdAndAsignatura_AsignaturaIdAndCargoAndBajaFalse(
                        request.docenteId(), request.asignaturaId(), request.cargo())) {
            throw new IllegalArgumentException(
                    "Este docente ya tiene una asignacion activa con ese cargo en esta asignatura");
        }

        DocenteAsignatura docenteAsignatura = DocenteAsignatura.builder()
                .docente(docente)
                .asignatura(asignatura)
                .cargo(request.cargo())
                .fechaDesde(request.fechaDesde())
                .fechaHasta(request.fechaHasta())
                .baja(false)
                .build();

        docenteAsignaturaRepositorio.save(docenteAsignatura);
        return DocenteAsignaturaResponseDTO.of(docenteAsignatura);
    }

    @Transactional(readOnly = true)
    public List<DocenteAsignaturaResponseDTO> listarPorAsignatura(UUID asignaturaId, boolean incluirBajas) {
        if (!asignaturaRepositorio.existsById(asignaturaId)) {
            throw new AsignaturaNoEncontradaException("No se encontro la asignatura con id " + asignaturaId);
        }
        return docenteAsignaturaRepositorio.findByAsignatura_AsignaturaId(asignaturaId).stream()
                .filter(docenteAsignatura -> incluirBajas || !docenteAsignatura.isBaja())
                .map(DocenteAsignaturaResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocenteAsignaturaResponseDTO> listarPorDocente(UUID docenteId, boolean incluirBajas) {
        if (!docenteRepositorio.existsById(docenteId)) {
            throw new DocenteNoEncontradoException("No se encontro el docente con id " + docenteId);
        }
        return docenteAsignaturaRepositorio.findByDocente_PersonaId(docenteId).stream()
                .filter(docenteAsignatura -> incluirBajas || !docenteAsignatura.isBaja())
                .map(DocenteAsignaturaResponseDTO::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocenteAsignaturaResponseDTO obtenerPorId(UUID docenteAsignaturaId) {
        return DocenteAsignaturaResponseDTO.of(buscarPorIdOLanzar(docenteAsignaturaId));
    }

    @Transactional
    public void darDeBaja(UUID docenteAsignaturaId) {
        DocenteAsignatura docenteAsignatura = buscarPorIdOLanzar(docenteAsignaturaId);
        docenteAsignatura.setBaja(true);
        docenteAsignatura.setFechaBaja(LocalDate.now());
    }

    private DocenteAsignatura buscarPorIdOLanzar(UUID docenteAsignaturaId) {
        return docenteAsignaturaRepositorio.findById(docenteAsignaturaId)
                .orElseThrow(() -> new DocenteAsignaturaNoEncontradaException(
                        "No se encontro la asignacion docente-asignatura con id " + docenteAsignaturaId));
    }
}
