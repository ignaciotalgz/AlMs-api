package com.algz.alms.dtos.invitacion;

import java.time.LocalDateTime;

import com.algz.alms.enumeraciones.TipoInvitacion;

public record InvitacionResponse(String token, TipoInvitacion tipo, LocalDateTime fechaExpiracion) {

}
