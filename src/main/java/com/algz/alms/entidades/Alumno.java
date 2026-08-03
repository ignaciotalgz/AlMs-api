package com.algz.alms.entidades;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
public class Alumno {
    @Id
    private UUID personaId;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "persona_id")
    private Persona persona;
    private String legajo;
    private LocalDate fechaAlta;
}
