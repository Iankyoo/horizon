package com.iankyoo.horizon.dto;

public record TaxaConversaoResponse(
        double aplicadoParaTriagem,
        double triagemParaEntrevista,
        double entrevistaParaOferta
) {
}
