package br.com.fiap.agendafit.dto;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.Enrollment;
import org.springframework.hateoas.server.core.Relation;

// Inscrição como ela sai da API, achatada com os dados da aula
@Relation(collectionRelation = "enrollments")
public record EnrollmentResponse(
        Long id,
        Long classId,
        String classTitle,
        LocalDateTime startsAt,
        LocalDateTime createdAt
) {

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getGymClass().getId(),
                enrollment.getGymClass().getTitle(),
                enrollment.getGymClass().getStartsAt(),
                enrollment.getCreatedAt());
    }
}
