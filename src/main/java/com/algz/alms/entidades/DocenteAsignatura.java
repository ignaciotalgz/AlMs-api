package com.algz.alms.entidades;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.algz.alms.enumeraciones.Cargo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteAsignatura {
    @jakarta.persistence.Id
    @UuidGenerator
    private UUID docenteAsignaturaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignatura_id", nullable = false)
    private Asignatura asignatura;
    @Enumerated(EnumType.STRING)
    private Cargo cargo;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private boolean baja;
    private LocalDate fechaBaja;
}
