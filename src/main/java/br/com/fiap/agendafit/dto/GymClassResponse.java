package br.com.fiap.agendafit.dto;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import org.springframework.hateoas.server.core.Relation;

/**
 * Aula como ela sai da API. Existe porque o contrato pede availableSpots,
 * que é calculado a partir das inscrições e não é coluna da entidade.
 *
 * Sem o @Relation a coleção sairia como _embedded.gymClassResponseList.
 */
@Relation(collectionRelation = "classes")
public record GymClassResponse(
        Long id,
        String title,
        Modality modality,
        Level level,
        String instructor,
        LocalDateTime startsAt,
        Integer durationMinutes,
        Integer capacity,
        Integer availableSpots
) {

    public static GymClassResponse fromEntity(GymClass gymClass, int availableSpots) {
        return new GymClassResponse(
                gymClass.getId(),
                gymClass.getTitle(),
                gymClass.getModality(),
                gymClass.getLevel(),
                gymClass.getInstructor(),
                gymClass.getStartsAt(),
                gymClass.getDurationMinutes(),
                gymClass.getCapacity(),
                availableSpots);
    }
}
