package com.iankyoo.horizon.repository;

import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.model.Vaga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    @Query("""
            SELECT v FROM Vaga v
            WHERE (:status IS NULL OR v.statusAtual = :status)
            AND (:plataforma IS NULL OR v.plataforma = :plataforma)
            """)
    Page<Vaga> findByFilters(@Param("status") StatusVaga status, @Param("plataforma") String plataforma, Pageable pageable);

}
