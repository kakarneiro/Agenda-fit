package br.com.fiap.agendafit.dto;

import jakarta.validation.constraints.NotBlank;

// Corpo do POST /auth/login
public record LoginRequest(
        @NotBlank(message = "email is required") String email,
        @NotBlank(message = "password is required") String password
) {
}
