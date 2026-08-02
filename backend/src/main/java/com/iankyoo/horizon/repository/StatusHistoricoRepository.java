package com.iankyoo.horizon.repository;

import com.iankyoo.horizon.model.StatusHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoricoRepository extends JpaRepository<StatusHistorico, Long> {

    List<StatusHistorico> findByVagaIdOrderByDataMudancaAsc(Long vagaId);

    List<StatusHistorico> findAllByOrderByVagaIdAscDataMudancaAsc();

}
