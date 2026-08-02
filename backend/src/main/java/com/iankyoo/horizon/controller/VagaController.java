package com.iankyoo.horizon.controller;

import com.iankyoo.horizon.dto.AtualizarStatusRequest;
import com.iankyoo.horizon.dto.VagaDetailResponse;
import com.iankyoo.horizon.dto.VagaRequest;
import com.iankyoo.horizon.dto.VagaResponse;
import com.iankyoo.horizon.enums.StatusVaga;
import com.iankyoo.horizon.service.VagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vagas")
@RequiredArgsConstructor
public class VagaController {

    private final VagaService vagaService;

    @PostMapping
    public ResponseEntity<VagaResponse> createVaga(@RequestBody @Valid VagaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vagaService.createVaga(request));
    }

    @GetMapping
    public ResponseEntity<Page<VagaResponse>> listVagas(
            @RequestParam(required = false) StatusVaga status,
            @RequestParam(required = false) String plataforma,
            Pageable pageable) {
        return ResponseEntity.ok(vagaService.listVagas(status, plataforma, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VagaDetailResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vagaService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VagaResponse> atualizarStatus(@PathVariable Long id, @RequestBody @Valid AtualizarStatusRequest request) {
        return ResponseEntity.ok(vagaService.atualizarStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVaga(@PathVariable Long id) {
        vagaService.deletarVaga(id);
        return ResponseEntity.noContent().build();
    }

}
