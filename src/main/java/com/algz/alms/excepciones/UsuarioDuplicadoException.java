package com.algz.alms.excepciones;

public class UsuarioDuplicadoException extends RuntimeException{
    public UsuarioDuplicadoException(String message){
        super(message);
    }
}
