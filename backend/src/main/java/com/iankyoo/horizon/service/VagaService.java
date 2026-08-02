package com.iankyoo.horizon.service;

import com.iankyoo.horizon.dto.AtualizarStatusRequest;
import com.iankyoo.horizon.dto.StatusHistoricoResponse;
import com.iankyoo.horizon.dto.VagaDetailResponse;
import com.iankyoo.horizon.dto.VagaRequest;
import com.iankyoo.horizon.dto.VagaResponse;
import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.exception.VagaNotFoundException;
import com.iankyoo.horizon.model.StatusHistorico;
import com.iankyoo.horizon.model.Vaga;
import com.iankyoo.horizon.repository.StatusHistoricoRepository;
import com.iankyoo.horizon.repository.VagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;
    private final StatusHistoricoRepository statusHistoricoRepository;

    private Vaga findVaga(Long id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new VagaNotFoundException(id));
    }

    private VagaResponse toResponse(Vaga vaga) {
        return new VagaResponse(
                vaga.getId(),
                vaga.getEmpresa(),
                vaga.getCargo(),
                vaga.getPlataforma(),
                vaga.getLink(),
                vaga.getStatusAtual(),
                vaga.getDataCriacao()
        );
    }

    @Transactional
    public VagaResponse createVaga(VagaRequest request) {
        Vaga vaga = Vaga.builder()
                .empresa(request.empresa())
                .cargo(request.cargo())
                .plataforma(request.plataforma())
                .link(request.link())
                .statusAtual(StatusVaga.APLICADO)
                .build();
        Vaga saved = vagaRepository.save(vaga);

        StatusHistorico historico = StatusHistorico.builder()
                .vaga(saved)
                .status(StatusVaga.APLICADO)
                .build();
        statusHistoricoRepository.save(historico);

        return toResponse(saved);
    }

    public Page<VagaResponse> listVagas(StatusVaga status, String plataforma, Pageable pageable) {
        return vagaRepository.findByFilters(status, plataforma, pageable)
                .map(this::toResponse);
    }

    public VagaDetailResponse findById(Long id) {
        Vaga vaga = findVaga(id);

        List<StatusHistoricoResponse> historico = statusHistoricoRepository
                .findByVagaIdOrderByDataMudancaAsc(id).stream()
                .map(h -> new StatusHistoricoResponse(h.getId(), h.getStatus(), h.getDataMudanca(), h.getObservacao()))
                .toList();

        return new VagaDetailResponse(
                vaga.getId(),
                vaga.getEmpresa(),
                vaga.getCargo(),
                vaga.getPlataforma(),
                vaga.getLink(),
                vaga.getStatusAtual(),
                vaga.getDataCriacao(),
                historico
        );
    }

    @Transactional
    public VagaResponse atualizarStatus(Long id, AtualizarStatusRequest request) {
        Vaga vaga = findVaga(id);

        vaga.setStatusAtual(request.status());
        Vaga saved = vagaRepository.save(vaga);

        StatusHistorico historico = StatusHistorico.builder()
                .vaga(saved)
                .status(request.status())
                .observacao(request.observacao())
                .build();
        statusHistoricoRepository.save(historico);

        return toResponse(saved);
    }

    public void deletarVaga(Long id) {
        Vaga vaga = findVaga(id);
        vaga.setArquivada(true);
        vagaRepository.save(vaga);
    }

}
