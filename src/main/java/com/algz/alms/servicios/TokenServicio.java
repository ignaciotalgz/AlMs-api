package com.algz.alms.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class TokenServicio {
    private static final SecureRandom GENERADOR_ALEATORIO = new SecureRandom();
    private static final int CANTIDAD_BYTES = 32;

    public String generarToken() {
        byte[] bytes = new byte[CANTIDAD_BYTES];
        GENERADOR_ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String valorCrudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(valorCrudo.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexBuilder = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en este entorno", e);
        }
    }
}
