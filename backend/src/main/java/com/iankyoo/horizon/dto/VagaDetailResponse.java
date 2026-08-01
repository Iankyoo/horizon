package com.iankyoo.horizon.dto;

import com.iankyoo.horizon.enums.StatusVaga;

import java.time.LocalDateTime;
import java.util.List;

public record VagaDetailResponse(
        Long id,
        String empresa,
        String cargo,
        String plataforma,
        String link,
        StatusVaga statusAtual,
        LocalDateTime dataCriacao,
        List<StatusHistoricoResponse> historico
) {
}
