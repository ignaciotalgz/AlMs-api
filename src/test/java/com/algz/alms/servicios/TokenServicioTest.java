package com.algz.alms.servicios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenServicio")
public class TokenServicioTest {
    private final TokenServicio tokenServicio = new TokenServicio();
    
    @Test
    @DisplayName("generarToken produce valores distintos en cada llamada")
    void generarToken_producesValoresDistintos() {
        String token1 = tokenServicio.generarToken();
        String token2 = tokenServicio.generarToken();
        assertThat(token1).isNotEqualTo(token2);
        assertThat(token1).isNotBlank();
        assertThat(token2).isNotBlank();
    }
    @Test
    @DisplayName("generarToken produce un valor Base64 URL-safe (sin + / =)")
    void generarToken_esUrlSafe() {
        String token = tokenServicio.generarToken();

        assertThat(token).doesNotContain("+", "/", "=");
    }

    @Test
    @DisplayName("hash es determinístico: el mismo valor produce siempre el mismo hash")
    void hash_esDeterministico() {
        String valor = "token-de-prueba-123";

        assertThat(tokenServicio.hash(valor)).isEqualTo(tokenServicio.hash(valor));
    }
    @Test
    @DisplayName("hash produce valores distintos para entradas distintas")
    void hash_valoresDistintosParaEntradasDistintas() {
        assertThat(tokenServicio.hash("valor-a")).isNotEqualTo(tokenServicio.hash("valor-b"));
    }

    @Test
    @DisplayName("hash produce un string hexadecimal de 64 caracteres (SHA-256)")
    void hash_tieneFormatoSha256() {
        String hash = tokenServicio.hash("cualquier-valor");

        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[0-9a-f]{64}");
    }
}
