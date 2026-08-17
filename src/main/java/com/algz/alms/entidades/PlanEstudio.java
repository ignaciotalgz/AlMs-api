package com.algz.alms.entidades;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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
public class PlanEstudio {
    @Id
    @UuidGenerator
    private UUID planEstudioId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;
    private String nombre;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private boolean baja;
}
