package com.algz.alms.entidades;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class Asignatura {
    @Id
    @UuidGenerator
    private UUID asignaturaId;
    private String nombre;
    private String codigo;
    private boolean baja;
}
