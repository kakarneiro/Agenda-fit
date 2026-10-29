package br.com.fiap.agendafit.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * Uma Specification por filtro, como pede o enunciado.
 * Cada método devolve apenas o seu próprio critério; quem junta tudo é o search().
 */
public final class GymClassSpecifications {

    private GymClassSpecifications() {
    }

    // title: contém, sem diferenciar maiúsculas de minúsculas
    public static Specification<GymClass> titleContains(String title) {
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<GymClass> modalityEquals(Modality modality) {
        return (root, query, builder) -> builder.equal(root.get("modality"), modality);
    }

    public static Specification<GymClass> levelEquals(Level level) {
        return (root, query, builder) -> builder.equal(root.get("level"), level);
    }

    public static Specification<GymClass> startsAtAfter(LocalDateTime startsAfter) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("startsAt"), startsAfter);
    }

    public static Specification<GymClass> startsAtBefore(LocalDateTime startsBefore) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("startsAt"), startsBefore);
    }

    /**
     * Só aulas com vaga: uma subquery conta as inscrições da aula e compara com a capacidade.
     * É a parte que o Query by Example não consegue fazer.
     */
    public static Specification<GymClass> onlyAvailable() {
        return (root, query, builder) -> {
            Subquery<Long> enrolled = query.subquery(Long.class);
            Root<Enrollment> enrollment = enrolled.from(Enrollment.class);
            enrolled.select(builder.count(enrollment));
            enrolled.where(builder.equal(enrollment.get("gymClass").get("id"), root.get("id")));

            return builder.lessThan(enrolled, root.get("capacity").as(Long.class));
        };
    }

    /**
     * Combina com "and" apenas os filtros que vieram preenchidos.
     * Começa de uma condição sempre verdadeira (conjunction) para que
     * uma busca sem nenhum filtro devolva todas as aulas.
     */
    public static Specification<GymClass> search(String title,
                                                 Modality modality,
                                                 Level level,
                                                 LocalDateTime startsAfter,
                                                 LocalDateTime startsBefore,
                                                 Boolean availableOnly) {

        List<Specification<GymClass>> filtrosInformados = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            filtrosInformados.add(titleContains(title));
        }
        if (modality != null) {
            filtrosInformados.add(modalityEquals(modality));
        }
        if (level != null) {
            filtrosInformados.add(levelEquals(level));
        }
        if (startsAfter != null) {
            filtrosInformados.add(startsAtAfter(startsAfter));
        }
        if (startsBefore != null) {
            filtrosInformados.add(startsAtBefore(startsBefore));
        }
        if (Boolean.TRUE.equals(availableOnly)) {
            filtrosInformados.add(onlyAvailable());
        }

        Specification<GymClass> combinada = (root, query, builder) -> builder.conjunction();
        for (Specification<GymClass> filtro : filtrosInformados) {
            combinada = combinada.and(filtro);
        }
        return combinada;
    }
}
