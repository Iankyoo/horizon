package com.iankyoo.horizon.exception;

import com.iankyoo.horizon.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundHandler_retorna404ComAMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> response = handler.notFoundHandler(new VagaNotFoundException(42L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("42");
    }

    @Test
    void unauthorizedHandler_retorna401ComAMensagemDaExcecao() {
        ResponseEntity<ErrorResponse> response = handler.unauthorizedHandler(new InvalidCredentialsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().message()).isEqualTo("Usuário ou senha inválidos");
    }

    @Test
    void validationHandler_retorna400ComUmaEntradaPorCampoInvalido() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("vagaRequest", "empresa", "não pode ficar em branco"),
                new FieldError("vagaRequest", "cargo", "não pode ficar em branco")
        ));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<Map<String, String>> response = handler.validationHandler(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("empresa", "não pode ficar em branco")
                .containsEntry("cargo", "não pode ficar em branco");
    }
}
