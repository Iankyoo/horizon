package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.AuthResponse;
import com.iankyoo.horizon.dto.LoginRequest;
import com.iankyoo.horizon.exception.InvalidCredentialsException;
import com.iankyoo.horizon.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(passwordEncoder, jwtService);
        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPasswordHash", "hash-bcrypt-fake");
    }

    @Test
    void login_comCredenciaisValidas_retornaTokenAssinadoParaOAdmin() {
        when(passwordEncoder.matches("senha-correta", "hash-bcrypt-fake")).thenReturn(true);
        when(jwtService.generateToken("admin")).thenReturn("token-gerado");

        AuthResponse response = authService.login(new LoginRequest("admin", "senha-correta"));

        assertThat(response.token()).isEqualTo("token-gerado");
    }

    @Test
    void login_comUsuarioInvalido_lancaExcecaoMasAindaAssimVerificaASenha() {
        when(passwordEncoder.matches("qualquer", "hash-bcrypt-fake")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("outro-usuario", "qualquer")));

        // decisão da issue #9: as duas checagens (usuário e senha) rodam sempre, mesmo
        // quando o usuário já está errado — evita vazar por tempo de resposta se o
        // usuário existe (short-circuit deixaria de chamar o BCrypt nesse caso)
        verify(passwordEncoder).matches("qualquer", "hash-bcrypt-fake");
    }

    @Test
    void login_comSenhaInvalida_lancaInvalidCredentialsException() {
        when(passwordEncoder.matches("senha-errada", "hash-bcrypt-fake")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("admin", "senha-errada")));
    }
}
