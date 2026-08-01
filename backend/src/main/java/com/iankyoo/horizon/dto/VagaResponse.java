package com.iankyoo.horizon.dto;

import com.iankyoo.horizon.enums.StatusVaga;

import java.time.LocalDateTime;

public record VagaResponse(
        Long id,
        String empresa,
        String cargo,
        String plataforma,
        String link,
        StatusVaga statusAtual,
        LocalDateTime dataCriacao
) {
}
