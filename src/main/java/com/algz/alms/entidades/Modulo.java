package com.algz.alms.entidades;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
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
public class Modulo {
    @jakarta.persistence.Id
    @UuidGenerator
    private UUID moduloId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_estudio_id", nullable = false)
    private PlanEstudio planEstudio;
    private String nombre;
    private Integer orden;
    private boolean baja;
}
