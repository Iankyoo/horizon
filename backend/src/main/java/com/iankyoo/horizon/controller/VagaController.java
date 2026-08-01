package com.iankyoo.horizon.controller;

import com.iankyoo.horizon.dto.VagaRequest;
import com.iankyoo.horizon.dto.VagaResponse;
import com.iankyoo.horizon.service.VagaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
