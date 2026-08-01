package com.iankyoo.horizon.exception;

public class VagaNotFoundException extends RuntimeException {
    public VagaNotFoundException(Long id) {
        super("Vaga não encontrada: id=" + id);
    }
}
