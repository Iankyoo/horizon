package com.iankyoo.horizon.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Placeholder temporário: CSRF desligado (API stateless, sem sessão/cookie) e
 * todas as rotas liberadas para permitir validar manualmente os endpoints
 * antes da autenticação JWT existir. Será substituído por completo na issue #9.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

}
