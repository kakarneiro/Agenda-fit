package br.com.fiap.agendafit.service;

import java.time.Instant;

import br.com.fiap.agendafit.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Emite o JWT assinado em HS256 com o jwt.secret do application.yml.
 *
 * Claims do contrato: sub (e-mail), name, role e exp.
 * O "role" é lido depois pelo JwtAuthenticationConverter da SecurityConfig
 * e vira a authority ROLE_ADMIN / ROLE_USER, que é o que o hasRole espera.
 */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final long expirationSeconds;

    public TokenService(JwtEncoder encoder,
                        @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        this.encoder = encoder;
        this.expirationSeconds = expirationSeconds;
    }

    public String generate(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("agenda-fit")
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
