package com.iankyoo.horizon.dto;

import com.iankyoo.horizon.enums.StatusVaga;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(
        @NotNull
        StatusVaga status,
        String observacao
) {
}
