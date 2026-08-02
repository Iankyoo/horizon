package com.iankyoo.horizon.model;

import com.iankyoo.horizon.enums.StatusVaga;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String empresa;

    @Column(nullable = false)
    private String cargo;

    private String plataforma;

    private String link;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVaga statusAtual;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    private boolean arquivada;

}
