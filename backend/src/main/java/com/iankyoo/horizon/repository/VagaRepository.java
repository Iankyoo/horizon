package com.iankyoo.horizon.repository;

import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.model.Vaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    @Query("""
            SELECT v FROM Vaga v
            WHERE v.arquivada = false
            AND (:status IS NULL OR v.statusAtual = :status)
            AND (:plataforma IS NULL OR v.plataforma = :plataforma)
            """)
    Page<Vaga> findByFilters(@Param("status") StatusVaga status, @Param("plataforma") String plataforma, Pageable pageable);

    long countByDataCriacaoGreaterThanEqual(LocalDateTime since);

    @Query("SELECT v.statusAtual, COUNT(v) FROM Vaga v GROUP BY v.statusAtual")
    List<Object[]> countGroupedByStatusAtual();

    @Query("""
            SELECT v.plataforma, COUNT(v) FROM Vaga v
            WHERE v.plataforma IS NOT NULL
            GROUP BY v.plataforma
            ORDER BY COUNT(v) DESC
            """)
    List<Object[]> countGroupedByPlataforma(Pageable pageable);

}
