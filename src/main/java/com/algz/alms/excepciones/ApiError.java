package com.algz.alms.excepciones;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ApiError(int status, String error, String mensaje, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timeStamp) {
    public static ApiError of(int status, String error, String mensaje){
        return new ApiError(status, error, mensaje, LocalDateTime.now());
    }
}
