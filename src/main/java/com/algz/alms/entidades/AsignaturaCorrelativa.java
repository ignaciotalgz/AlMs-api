package com.algz.alms.entidades;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.algz.alms.enumeraciones.TipoCorrelatividad;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class AsignaturaCorrelativa {
    @Id
    @UuidGenerator
    private UUID asignaturaCorrelativaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_asignatura_id", nullable = false)
    private ModuloAsignatura moduloAsignatura;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_asignatura_requerida_id", nullable = false)
    private ModuloAsignatura moduloAsignaturaRequerida;
    @Enumerated(EnumType.STRING)
    private TipoCorrelatividad tipoCorrelatividad;
    private boolean baja;    
}
