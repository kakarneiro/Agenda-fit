package br.com.fiap.agendafit.security;

import java.util.Optional;

import br.com.fiap.agendafit.model.Role;
import br.com.fiap.agendafit.model.User;
import br.com.fiap.agendafit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Descobre quem está logado a partir do SecurityContext.
 *
 * É usado pelos assemblers (para decidir quais links aparecem) e pelos
 * services (para a regra de dono das inscrições).
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUser {

    private final UserRepository userRepository;

    /** Vazio quando a requisição veio sem token. */
    public Optional<User> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }

        // o "sub" do token é o e-mail
        return userRepository.findByEmail(jwt.getSubject());
    }

    /** Para quando a rota exige login; a SecurityConfig já barra antes, isto é a rede de proteção. */
    public User require() {
        return current().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    public boolean isLoggedIn() {
        return current().isPresent();
    }

    public boolean isAdmin() {
        return current().map(user -> user.getRole() == Role.ADMIN).orElse(false);
    }
}
