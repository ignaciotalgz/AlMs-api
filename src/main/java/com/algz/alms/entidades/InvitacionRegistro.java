package com.algz.alms.entidades;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.algz.alms.enumeraciones.Rol;

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
public class InvitacionRegistro {
    @Id
    @UuidGenerator
    private UUID invitacionId;
    private String tokenHash;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    private boolean usada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaUso;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_usuario_id")
    private Usuario creadaPor;
}
