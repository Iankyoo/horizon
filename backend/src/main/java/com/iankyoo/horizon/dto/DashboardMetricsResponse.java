package com.iankyoo.horizon.dto;

import com.iankyoo.horizon.enums.StatusVaga;

import java.util.List;
import java.util.Map;

public record DashboardMetricsResponse(
        long totalVagas,
        long totalUltimos30Dias,
        Map<StatusVaga, Long> distribuicaoPorStatus,
        TaxaConversaoResponse taxaConversao,
        Map<StatusVaga, Double> tempoMedioPorEtapaEmDias,
        List<PlataformaCountResponse> topPlataformas
) {
}
