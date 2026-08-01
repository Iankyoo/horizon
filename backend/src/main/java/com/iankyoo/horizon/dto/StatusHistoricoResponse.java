package com.iankyoo.horizon.dto;

import com.iankyoo.horizon.enums.StatusVaga;

import java.time.LocalDateTime;

public record StatusHistoricoResponse(
        Long id,
        StatusVaga status,
        LocalDateTime dataMudanca,
        String observacao
) {
}
