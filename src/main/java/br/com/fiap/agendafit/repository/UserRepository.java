package br.com.fiap.agendafit.repository;

import java.util.Optional;

import br.com.fiap.agendafit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // usado no login e para descobrir quem está logado a partir do "sub" do token
    Optional<User> findByEmail(String email);

    // usado no cadastro, para devolver 409 quando o e-mail já existe
    boolean existsByEmail(String email);
}
