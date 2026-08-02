package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.AuthResponse;
import com.iankyoo.horizon.dto.LoginRequest;
import com.iankyoo.horizon.exception.InvalidCredentialsException;
import com.iankyoo.horizon.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password-hash}")
    private String adminPasswordHash;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        boolean usuarioValido = adminUsername.equals(request.username());
        boolean senhaValida = passwordEncoder.matches(request.password(), adminPasswordHash);

        if (!usuarioValido || !senhaValida) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(adminUsername);
        return new AuthResponse(token);
    }

}
