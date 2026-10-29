package br.com.fiap.agendafit.service;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Role;
import br.com.fiap.agendafit.model.User;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository repository;
    private final GymClassService gymClassService;
    private final AuthenticatedUser authenticatedUser;

    /**
     * Inscreve o usuário logado na aula.
     * 404 se a aula não existe, 409 se já está lotada ou se ele já está inscrito.
     */
    @Transactional
    public Enrollment enroll(Long classId) {
        User user = authenticatedUser.require();
        GymClass gymClass = gymClassService.findById(classId);

        if (repository.existsByUserIdAndGymClassId(user.getId(), classId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this class");
        }
        if (gymClassService.availableSpots(gymClass) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Class is full");
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .gymClass(gymClass)
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(enrollment);
    }

    /** O aluno vê apenas as próprias inscrições. */
    public Page<Enrollment> findMine(Pageable pageable) {
        User user = authenticatedUser.require();
        return repository.findByUserId(user.getId(), pageable);
    }

    /** Regra de dono: o aluno cancela só as próprias; o ADMIN cancela qualquer uma. */
    @Transactional
    public void cancel(Long enrollmentId) {
        User user = authenticatedUser.require();

        Enrollment enrollment = repository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));

        if (!canCancel(user, enrollment)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        repository.delete(enrollment);
    }

    public boolean canCancel(User user, Enrollment enrollment) {
        boolean isOwner = enrollment.getUser().getId().equals(user.getId());
        return isOwner || user.getRole() == Role.ADMIN;
    }
}
