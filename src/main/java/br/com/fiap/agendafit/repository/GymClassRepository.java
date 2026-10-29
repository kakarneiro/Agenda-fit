package br.com.fiap.agendafit.repository;

import br.com.fiap.agendafit.model.GymClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaRepository já traz a paginação (findAll(Pageable)) e o Query by Example
 * (findAll(Example, Pageable)).
 *
 * JpaSpecificationExecutor é o que habilita a busca por Specification,
 * usada no /classes/search para faixas de horário, "contém" e vagas.
 */
public interface GymClassRepository extends JpaRepository<GymClass, Long>,
                                            JpaSpecificationExecutor<GymClass> {
}
