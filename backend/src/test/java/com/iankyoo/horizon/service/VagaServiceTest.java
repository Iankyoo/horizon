package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.AtualizarStatusRequest;
import com.iankyoo.horizon.dto.VagaDetailResponse;
import com.iankyoo.horizon.dto.VagaRequest;
import com.iankyoo.horizon.dto.VagaResponse;
import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.exception.VagaNotFoundException;
import com.iankyoo.horizon.model.StatusHistorico;
import com.iankyoo.horizon.model.Vaga;
import com.iankyoo.horizon.repository.StatusHistoricoRepository;
import com.iankyoo.horizon.repository.VagaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private StatusHistoricoRepository statusHistoricoRepository;

    @InjectMocks
    private VagaService vagaService;

    @Test
    void createVaga_salvaVagaComoAplicadoERegistraPrimeiroEventoDoHistorico() {
        VagaRequest request = new VagaRequest("Itau", "Dev Java", "Gupy", "https://x.com/1");
        Vaga salva = Vaga.builder()
                .id(1L)
                .empresa("Itau")
                .cargo("Dev Java")
                .plataforma("Gupy")
                .link("https://x.com/1")
                .statusAtual(StatusVaga.APLICADO)
                .dataCriacao(LocalDateTime.now())
                .build();
        when(vagaRepository.save(any(Vaga.class))).thenReturn(salva);

        VagaResponse response = vagaService.createVaga(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.statusAtual()).isEqualTo(StatusVaga.APLICADO);

        ArgumentCaptor<StatusHistorico> historicoCaptor = ArgumentCaptor.forClass(StatusHistorico.class);
        verify(statusHistoricoRepository).save(historicoCaptor.capture());
        StatusHistorico historico = historicoCaptor.getValue();
        assertThat(historico.getStatus()).isEqualTo(StatusVaga.APLICADO);
        assertThat(historico.getVaga()).isEqualTo(salva);
        assertThat(historico.getObservacao()).isNull();
    }

    @Test
    void listVagas_delegaParaRepositorioEMapeiaParaResponse() {
        Vaga vaga = Vaga.builder().id(1L).empresa("Nubank").cargo("Backend").statusAtual(StatusVaga.TRIAGEM).build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Vaga> pagina = new PageImpl<>(List.of(vaga), pageable, 1);
        when(vagaRepository.findByFilters(StatusVaga.TRIAGEM, "LinkedIn", pageable)).thenReturn(pagina);

        Page<VagaResponse> resultado = vagaService.listVagas(StatusVaga.TRIAGEM, "LinkedIn", pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).empresa()).isEqualTo("Nubank");
    }

    @Test
    void findById_vagaExistente_retornaDetalheComHistoricoOrdenado() {
        Vaga vaga = Vaga.builder().id(5L).empresa("C6 Bank").cargo("Dev").statusAtual(StatusVaga.ENTREVISTA)
                .dataCriacao(LocalDateTime.now()).build();
        when(vagaRepository.findById(5L)).thenReturn(Optional.of(vaga));

        StatusHistorico e1 = StatusHistorico.builder().id(1L).vaga(vaga).status(StatusVaga.APLICADO)
                .dataMudanca(LocalDateTime.now().minusDays(2)).build();
        StatusHistorico e2 = StatusHistorico.builder().id(2L).vaga(vaga).status(StatusVaga.ENTREVISTA)
                .dataMudanca(LocalDateTime.now()).observacao("obs").build();
        when(statusHistoricoRepository.findByVagaIdOrderByDataMudancaAsc(5L)).thenReturn(List.of(e1, e2));

        VagaDetailResponse response = vagaService.findById(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.historico()).hasSize(2);
        assertThat(response.historico().get(1).observacao()).isEqualTo("obs");
    }

    @Test
    void findById_vagaInexistente_lancaVagaNotFoundException() {
        when(vagaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VagaNotFoundException.class, () -> vagaService.findById(99L));
    }

    @Test
    void atualizarStatus_vagaExistente_atualizaStatusERegistraNovoEventoDoHistorico() {
        Vaga vaga = Vaga.builder().id(7L).empresa("Santander").cargo("Dev").statusAtual(StatusVaga.APLICADO).build();
        when(vagaRepository.findById(7L)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(vaga)).thenReturn(vaga);

        AtualizarStatusRequest request = new AtualizarStatusRequest(StatusVaga.TRIAGEM, "Recrutador retornou");
        VagaResponse response = vagaService.atualizarStatus(7L, request);

        assertThat(response.statusAtual()).isEqualTo(StatusVaga.TRIAGEM);
        assertThat(vaga.getStatusAtual()).isEqualTo(StatusVaga.TRIAGEM);

        ArgumentCaptor<StatusHistorico> captor = ArgumentCaptor.forClass(StatusHistorico.class);
        verify(statusHistoricoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusVaga.TRIAGEM);
        assertThat(captor.getValue().getObservacao()).isEqualTo("Recrutador retornou");
    }

    @Test
    void atualizarStatus_permiteQualquerTransicaoSemMaquinaDeEstado() {
        // decisão das issues #6/#7: sem validação de transição — de OFERTA direto pra REJEITADO é válido
        Vaga vaga = Vaga.builder().id(8L).empresa("BTG").cargo("Dev").statusAtual(StatusVaga.OFERTA).build();
        when(vagaRepository.findById(8L)).thenReturn(Optional.of(vaga));
        when(vagaRepository.save(vaga)).thenReturn(vaga);

        VagaResponse response = vagaService.atualizarStatus(8L, new AtualizarStatusRequest(StatusVaga.REJEITADO, null));

        assertThat(response.statusAtual()).isEqualTo(StatusVaga.REJEITADO);
    }

    @Test
    void atualizarStatus_vagaInexistente_lancaVagaNotFoundExceptionSemTocarHistorico() {
        when(vagaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(VagaNotFoundException.class,
                () -> vagaService.atualizarStatus(404L, new AtualizarStatusRequest(StatusVaga.TRIAGEM, null)));

        verify(statusHistoricoRepository, never()).save(any());
    }

    @Test
    void deletarVaga_vagaExistente_marcaComoArquivadaSemApagarLinha() {
        Vaga vaga = Vaga.builder().id(3L).empresa("Inter").cargo("Dev").statusAtual(StatusVaga.TRIAGEM)
                .arquivada(false).build();
        when(vagaRepository.findById(3L)).thenReturn(Optional.of(vaga));

        vagaService.deletarVaga(3L);

        assertThat(vaga.isArquivada()).isTrue();
        verify(vagaRepository).save(vaga);
        verify(vagaRepository, never()).deleteById(any());
    }

    @Test
    void deletarVaga_vagaInexistente_lancaVagaNotFoundException() {
        when(vagaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(VagaNotFoundException.class, () -> vagaService.deletarVaga(404L));
    }
}
