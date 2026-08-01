package com.iankyoo.horizon.dto;

import jakarta.validation.constraints.NotBlank;

public record VagaRequest(
        @NotBlank
        String empresa,
        @NotBlank
        String cargo,
        String plataforma,
        String link
) {
}
