package br.com.fiap.agendafit.repository;

import br.com.fiap.agendafit.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    void deleteByGymClassId(Long gymClassId);

    // quantas vagas já foram ocupadas numa aula
    long countByGymClassId(Long gymClassId);

    // impede inscrição duplicada e decide se o link "enroll" aparece
    boolean existsByUserIdAndGymClassId(Long userId, Long gymClassId);

    // GET /enrollments/me
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);
}
