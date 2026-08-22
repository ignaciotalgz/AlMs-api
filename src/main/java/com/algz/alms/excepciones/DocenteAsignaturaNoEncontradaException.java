package com.algz.alms.excepciones;

public class DocenteAsignaturaNoEncontradaException extends RuntimeException {
    public DocenteAsignaturaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}