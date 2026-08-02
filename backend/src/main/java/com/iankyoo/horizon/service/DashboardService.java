package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.DashboardMetricsResponse;
import com.iankyoo.horizon.dto.PlataformaCountResponse;
import com.iankyoo.horizon.dto.TaxaConversaoResponse;
import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.model.StatusHistorico;
import com.iankyoo.horizon.repository.StatusHistoricoRepository;
import com.iankyoo.horizon.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DIAS_PERIODO_RECENTE = 30;
    private static final int TOP_PLATAFORMAS_LIMITE = 5;

    private final VagaRepository vagaRepository;
    private final StatusHistoricoRepository statusHistoricoRepository;

    public DashboardMetricsResponse getMetrics() {
        long totalVagas = vagaRepository.count();
        long totalUltimosPeriodo = vagaRepository.countByDataCriacaoGreaterThanEqual(
                LocalDateTime.now().minusDays(DIAS_PERIODO_RECENTE));

        Map<StatusVaga, Long> distribuicaoPorStatus = distribuicaoPorStatus();

        List<StatusHistorico> historico = statusHistoricoRepository.findAllByOrderByVagaIdAscDataMudancaAsc();
        Map<StatusVaga, Long> vagasQueAtingiram = new EnumMap<>(StatusVaga.class);
        Map<StatusVaga, List<Double>> duracoesEmDiasPorStatus = new EnumMap<>(StatusVaga.class);
        for (StatusVaga status : StatusVaga.values()) {
            vagasQueAtingiram.put(status, 0L);
            duracoesEmDiasPorStatus.put(status, new ArrayList<>());
        }

        Long vagaAtualId = null;
        Set<StatusVaga> statusJaVistos = null;
        StatusHistorico eventoAnterior = null;

        for (StatusHistorico evento : historico) {
            Long vagaId = evento.getVaga().getId();
            if (!vagaId.equals(vagaAtualId)) {
                vagaAtualId = vagaId;
                statusJaVistos = new HashSet<>();
                eventoAnterior = null;
            }

            if (statusJaVistos.add(evento.getStatus())) {
                vagasQueAtingiram.merge(evento.getStatus(), 1L, Long::sum);
            }

            if (eventoAnterior != null) {
                double dias = Duration.between(eventoAnterior.getDataMudanca(), evento.getDataMudanca()).toMinutes() / 1440.0;
                duracoesEmDiasPorStatus.get(eventoAnterior.getStatus()).add(dias);
            }

            eventoAnterior = evento;
        }

        Map<StatusVaga, Double> tempoMedioPorEtapa = new EnumMap<>(StatusVaga.class);
        for (StatusVaga status : StatusVaga.values()) {
            List<Double> duracoes = duracoesEmDiasPorStatus.get(status);
            double media = duracoes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            tempoMedioPorEtapa.put(status, arredondar(media));
        }

        TaxaConversaoResponse taxaConversao = new TaxaConversaoResponse(
                percentual(vagasQueAtingiram.get(StatusVaga.TRIAGEM), vagasQueAtingiram.get(StatusVaga.APLICADO)),
                percentual(vagasQueAtingiram.get(StatusVaga.ENTREVISTA), vagasQueAtingiram.get(StatusVaga.TRIAGEM)),
                percentual(vagasQueAtingiram.get(StatusVaga.OFERTA), vagasQueAtingiram.get(StatusVaga.ENTREVISTA))
        );

        List<PlataformaCountResponse> topPlataformas = vagaRepository
                .countGroupedByPlataforma(PageRequest.of(0, TOP_PLATAFORMAS_LIMITE)).stream()
                .map(row -> new PlataformaCountResponse((String) row[0], (Long) row[1]))
                .toList();

        return new DashboardMetricsResponse(
                totalVagas,
                totalUltimosPeriodo,
                distribuicaoPorStatus,
                taxaConversao,
                tempoMedioPorEtapa,
                topPlataformas
        );
    }

    private Map<StatusVaga, Long> distribuicaoPorStatus() {
        Map<StatusVaga, Long> distribuicao = new EnumMap<>(StatusVaga.class);
        for (StatusVaga status : StatusVaga.values()) {
            distribuicao.put(status, 0L);
        }
        for (Object[] linha : vagaRepository.countGroupedByStatusAtual()) {
            distribuicao.put((StatusVaga) linha[0], (Long) linha[1]);
        }
        return distribuicao;
    }

    private double percentual(long numerador, long denominador) {
        if (denominador == 0) {
            return 0.0;
        }
        return arredondar((numerador * 100.0) / denominador);
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

}
