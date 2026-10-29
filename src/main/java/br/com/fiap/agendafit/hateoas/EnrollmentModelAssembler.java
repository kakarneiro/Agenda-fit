package br.com.fiap.agendafit.hateoas;

import br.com.fiap.agendafit.controller.EnrollmentController;
import br.com.fiap.agendafit.controller.GymClassController;
import br.com.fiap.agendafit.dto.EnrollmentResponse;
import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.security.AuthenticatedUser;
import br.com.fiap.agendafit.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Monta a inscrição como EntityModel.
 *   class  -> sempre (a aula da inscrição)
 *   cancel -> só para o dono da inscrição ou para o ADMIN
 */
@Component
@RequiredArgsConstructor
public class EnrollmentModelAssembler implements RepresentationModelAssembler<Enrollment, EntityModel<EnrollmentResponse>> {

    private final AuthenticatedUser authenticatedUser;
    private final EnrollmentService enrollmentService;

    @Override
    public EntityModel<EnrollmentResponse> toModel(Enrollment enrollment) {
        EntityModel<EnrollmentResponse> model = EntityModel.of(
                EnrollmentResponse.fromEntity(enrollment),
                linkTo(methodOn(GymClassController.class).findById(enrollment.getGymClass().getId())).withRel("class"));

        authenticatedUser.current()
                .filter(user -> enrollmentService.canCancel(user, enrollment))
                .ifPresent(user -> model.add(
                        linkTo(methodOn(EnrollmentController.class).cancel(enrollment.getId())).withRel("cancel")));

        return model;
    }
}
