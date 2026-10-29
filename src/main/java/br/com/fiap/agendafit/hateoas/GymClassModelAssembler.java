package br.com.fiap.agendafit.hateoas;

import br.com.fiap.agendafit.controller.EnrollmentController;
import br.com.fiap.agendafit.controller.GymClassController;
import br.com.fiap.agendafit.dto.GymClassResponse;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.User;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.security.AuthenticatedUser;
import br.com.fiap.agendafit.service.GymClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Monta a aula como EntityModel e decide quais links aparecem,
 * consultando quem está logado a cada requisição.
 *
 * O app mostra um botão só quando o link dele veio na resposta, então
 * é o servidor que diz o que aquele usuário pode fazer:
 *   self e classes -> sempre
 *   enroll         -> logado, com vaga e ainda não inscrito
 *   update/delete  -> apenas ADMIN
 */
@Component
@RequiredArgsConstructor
public class GymClassModelAssembler implements RepresentationModelAssembler<GymClass, EntityModel<GymClassResponse>> {

    private final GymClassService gymClassService;
    private final EnrollmentRepository enrollmentRepository;
    private final AuthenticatedUser authenticatedUser;

    @Override
    public EntityModel<GymClassResponse> toModel(GymClass gymClass) {
        int availableSpots = gymClassService.availableSpots(gymClass);

        EntityModel<GymClassResponse> model = EntityModel.of(
                GymClassResponse.fromEntity(gymClass, availableSpots),
                linkTo(methodOn(GymClassController.class).findById(gymClass.getId())).withSelfRel(),
                linkTo(methodOn(GymClassController.class).findAll(null)).withRel("classes"));

        Optional<User> loggedUser = authenticatedUser.current();

        if (loggedUser.isPresent() && availableSpots > 0
                && !enrollmentRepository.existsByUserIdAndGymClassId(loggedUser.get().getId(), gymClass.getId())) {
            model.add(linkTo(methodOn(EnrollmentController.class).enroll(gymClass.getId())).withRel("enroll"));
        }

        if (authenticatedUser.isAdmin()) {
            model.add(linkTo(methodOn(GymClassController.class).update(gymClass.getId(), null)).withRel("update"));
            model.add(linkTo(methodOn(GymClassController.class).delete(gymClass.getId())).withRel("delete"));
        }

        return model;
    }
}
