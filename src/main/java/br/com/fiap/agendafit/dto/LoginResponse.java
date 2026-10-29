package br.com.fiap.agendafit.dto;

// Resposta do POST /auth/login, no formato do contrato
public record LoginResponse(String token, long expiresIn) {
}
