package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.DashboardMetricsResponse;
import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.model.StatusHistorico;
import com.iankyoo.horizon.model.Vaga;
import com.iankyoo.horizon.repository.StatusHistoricoRepository;
import com.iankyoo.horizon.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private StatusHistoricoRepository statusHistoricoRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(vagaRepository, statusHistoricoRepository);
        // defaults neutros — cada teste sobrescreve só o que precisa; lenient() porque
        // um teste que sobrescreve um destes stubs faz o default "sumir" aos olhos do
        // Mockito (o stub mais recente vence), o que o modo estrito trataria como stub
        // morto mesmo sendo, na prática, o fallback intencional dos outros testes
        lenient().when(vagaRepository.count()).thenReturn(0L);
        lenient().when(vagaRepository.countByDataCriacaoGreaterThanEqual(any())).thenReturn(0L);
        lenient().when(vagaRepository.countGroupedByStatusAtual()).thenReturn(List.of());
        lenient().when(vagaRepository.countGroupedByPlataforma(any(Pageable.class))).thenReturn(List.of());
        lenient().when(statusHistoricoRepository.findAllByOrderByVagaIdAscDataMudancaAsc()).thenReturn(List.of());
    }

    private StatusHistorico evento(Vaga vaga, StatusVaga status, LocalDateTime dataMudanca) {
        return StatusHistorico.builder().vaga(vaga).status(status).dataMudanca(dataMudanca).build();
    }

    @Test
    void getMetrics_totalEDistribuicaoPorStatus_incluiStatusZeradosNoMapa() {
        when(vagaRepository.count()).thenReturn(3L);
        when(vagaRepository.countByDataCriacaoGreaterThanEqual(any())).thenReturn(2L);
        when(vagaRepository.countGroupedByStatusAtual()).thenReturn(List.of(
                new Object[]{StatusVaga.APLICADO, 2L},
                new Object[]{StatusVaga.ENTREVISTA, 1L}
        ));

        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        assertThat(metrics.totalVagas()).isEqualTo(3L);
        assertThat(metrics.totalUltimos30Dias()).isEqualTo(2L);
        assertThat(metrics.distribuicaoPorStatus())
                .containsEntry(StatusVaga.APLICADO, 2L)
                .containsEntry(StatusVaga.ENTREVISTA, 1L)
                .containsEntry(StatusVaga.TRIAGEM, 0L)
                .containsEntry(StatusVaga.OFERTA, 0L)
                .containsEntry(StatusVaga.REJEITADO, 0L);
    }

    @Test
    void getMetrics_calculaTaxaDeConversaoEntreEtapasAPartirDoHistoricoReal() {
        LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 10, 0);
        Vaga vagaA = Vaga.builder().id(1L).build();
        Vaga vagaB = Vaga.builder().id(2L).build();
        Vaga vagaC = Vaga.builder().id(3L).build();

        // A: aplicado -> triagem -> entrevista (parada em entrevista)
        // B: aplicado -> triagem -> rejeitado
        // C: aplicado (parada, nunca saiu)
        List<StatusHistorico> historico = List.of(
                evento(vagaA, StatusVaga.APLICADO, t0),
                evento(vagaA, StatusVaga.TRIAGEM, t0.plusDays(3)),
                evento(vagaA, StatusVaga.ENTREVISTA, t0.plusDays(10)),
                evento(vagaB, StatusVaga.APLICADO, t0),
                evento(vagaB, StatusVaga.TRIAGEM, t0.plusDays(5)),
                evento(vagaB, StatusVaga.REJEITADO, t0.plusDays(8)),
                evento(vagaC, StatusVaga.APLICADO, t0)
        );
        when(statusHistoricoRepository.findAllByOrderByVagaIdAscDataMudancaAsc()).thenReturn(historico);

        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        // aplicadoParaTriagem: 2 de 3 vagas chegaram em TRIAGEM
        assertThat(metrics.taxaConversao().aplicadoParaTriagem()).isEqualTo(66.67);
        // triagemParaEntrevista: 1 de 2 vagas que passaram por TRIAGEM chegou em ENTREVISTA
        assertThat(metrics.taxaConversao().triagemParaEntrevista()).isEqualTo(50.0);
        // entrevistaParaOferta: 0 de 1 vaga que chegou em ENTREVISTA foi pra OFERTA
        assertThat(metrics.taxaConversao().entrevistaParaOferta()).isEqualTo(0.0);
    }

    @Test
    void getMetrics_calculaTempoMedioPorEtapaSoContandoTransicoesQueDeFatoAconteceram() {
        LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 10, 0);
        Vaga vagaA = Vaga.builder().id(1L).build();
        Vaga vagaB = Vaga.builder().id(2L).build();

        List<StatusHistorico> historico = List.of(
                evento(vagaA, StatusVaga.APLICADO, t0),
                evento(vagaA, StatusVaga.TRIAGEM, t0.plusDays(3)),     // APLICADO durou 3 dias
                evento(vagaA, StatusVaga.ENTREVISTA, t0.plusDays(10)), // TRIAGEM durou 7 dias; ENTREVISTA fica sem próximo evento
                evento(vagaB, StatusVaga.APLICADO, t0),
                evento(vagaB, StatusVaga.TRIAGEM, t0.plusDays(5)),     // APLICADO durou 5 dias
                evento(vagaB, StatusVaga.REJEITADO, t0.plusDays(8))    // TRIAGEM durou 3 dias
        );
        when(statusHistoricoRepository.findAllByOrderByVagaIdAscDataMudancaAsc()).thenReturn(historico);

        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.APLICADO)).isEqualTo(4.0);  // media(3,5)
        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.TRIAGEM)).isEqualTo(5.0);   // media(7,3)
        // ENTREVISTA/OFERTA/REJEITADO são pontos de chegada nesse cenário: ninguém saiu deles, média fica 0
        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.ENTREVISTA)).isEqualTo(0.0);
        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.OFERTA)).isEqualTo(0.0);
        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.REJEITADO)).isEqualTo(0.0);
    }

    @Test
    void getMetrics_semHistorico_taxaDeConversaoZeradaSemDivisaoPorZero() {
        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        assertThat(metrics.taxaConversao().aplicadoParaTriagem()).isEqualTo(0.0);
        assertThat(metrics.taxaConversao().triagemParaEntrevista()).isEqualTo(0.0);
        assertThat(metrics.taxaConversao().entrevistaParaOferta()).isEqualTo(0.0);
    }

    @Test
    void getMetrics_naoMisturaHistoricoDeVagasDiferentesAoCalcularDuracao() {
        // guarda contra regressão: o evento de uma vaga não pode virar "duração" contra
        // o evento anterior de OUTRA vaga só porque a lista veio concatenada
        LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 10, 0);
        Vaga vagaA = Vaga.builder().id(1L).build();
        Vaga vagaB = Vaga.builder().id(2L).build();

        List<StatusHistorico> historico = List.of(
                evento(vagaA, StatusVaga.APLICADO, t0),
                // vagaB começa muito depois; se o código comparasse (erradamente) com o evento
                // anterior de vagaA, a "duração" do APLICADO da vagaA seria de 40 dias
                evento(vagaB, StatusVaga.APLICADO, t0.plusDays(40))
        );
        when(statusHistoricoRepository.findAllByOrderByVagaIdAscDataMudancaAsc()).thenReturn(historico);

        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        assertThat(metrics.tempoMedioPorEtapaEmDias().get(StatusVaga.APLICADO)).isEqualTo(0.0);
    }

    @Test
    void getMetrics_mapeiaTopPlataformasNaOrdemDoRepositorio() {
        when(vagaRepository.countGroupedByPlataforma(any(Pageable.class))).thenReturn(List.of(
                new Object[]{"Gupy", 5L},
                new Object[]{"LinkedIn", 3L}
        ));

        DashboardMetricsResponse metrics = dashboardService.getMetrics();

        assertThat(metrics.topPlataformas()).hasSize(2);
        assertThat(metrics.topPlataformas().get(0).plataforma()).isEqualTo("Gupy");
        assertThat(metrics.topPlataformas().get(0).total()).isEqualTo(5L);
        assertThat(metrics.topPlataformas().get(1).plataforma()).isEqualTo("LinkedIn");
    }
}
