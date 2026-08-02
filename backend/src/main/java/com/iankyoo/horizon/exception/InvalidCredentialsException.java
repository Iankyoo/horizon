package com.iankyoo.horizon.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Usuário ou senha inválidos");
    }
}
