package com.algz.alms.enumeraciones;

public enum TipoInvitacion {
    ALUMNO(Rol.ROLE_ALUMNO),
    DOCENTE(Rol.ROLE_DOCENTE);

    private final Rol rolAsociado;
    TipoInvitacion(Rol rolAsociado){
        this.rolAsociado = rolAsociado;
    }
    public Rol getRolAsociado() {
        return rolAsociado;
    }
}
