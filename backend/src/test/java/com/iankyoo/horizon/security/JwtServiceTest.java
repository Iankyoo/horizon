package com.iankyoo.horizon.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // secret só de uso nos testes, sem relação com nenhum ambiente real do projeto
    private static final String SECRET = "dGVzdGUtc2VjcmV0LWFwZW5hcy1wYXJhLW9zLXRlc3Rlcy1qd3QtdW5pdGFyaW8=";
    private static final String OUTRO_SECRET = "b3V0cmEtY2hhdmUtc2VjcmV0YS1jb21wbGV0YW1lbnRlLWRpZmVyZW50ZS0wMQ==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", 86_400_000L);
    }

    @Test
    void generateTokenEGetUsername_fazemRoundtripDoSubject() {
        String token = jwtService.generateToken("admin");

        assertThat(jwtService.getUsername(token)).isEqualTo("admin");
    }

    @Test
    void extractClaims_expirationFicaDepoisDoIssuedAt() {
        String token = jwtService.generateToken("admin");

        Claims claims = jwtService.extractClaims(token);

        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void isTokenValid_tokenRecemGerado_retornaTrue() {
        String token = jwtService.generateToken("admin");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_tokenExpirado_retornaFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1_000L);
        String tokenExpirado = jwtService.generateToken("admin");

        assertThat(jwtService.isTokenValid(tokenExpirado)).isFalse();
    }

    @Test
    void isTokenValid_tokenMalFormado_retornaFalseEmVezDeLancarExcecao() {
        assertThat(jwtService.isTokenValid("isso-nao-e-um-jwt")).isFalse();
    }

    @Test
    void isTokenValid_tokenAssinadoComOutraChave_retornaFalse() {
        JwtService outroServico = new JwtService();
        ReflectionTestUtils.setField(outroServico, "secretKey", OUTRO_SECRET);
        ReflectionTestUtils.setField(outroServico, "expiration", 86_400_000L);
        String tokenComOutraAssinatura = outroServico.generateToken("admin");

        assertThat(jwtService.isTokenValid(tokenComOutraAssinatura)).isFalse();
    }
}
