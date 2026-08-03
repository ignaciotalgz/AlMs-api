package com.algz.alms.dtos.invitacion;

import com.algz.alms.enumeraciones.TipoInvitacion;

import jakarta.validation.constraints.NotNull;

public record InvitacionRequest(@NotNull(message = "El tipo de invitacion es obligatorio") TipoInvitacion tipo) {

}
