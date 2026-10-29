package br.com.fiap.agendafit.service;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.dto.GymClassRequest;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import br.com.fiap.agendafit.repository.EnrollmentRepository;
import br.com.fiap.agendafit.repository.GymClassRepository;
import br.com.fiap.agendafit.repository.GymClassSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GymClassService {

    private final GymClassRepository repository;
    private final EnrollmentRepository enrollmentRepository;

    public Page<GymClass> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    /**
     * Query by Example: monta uma "aula de exemplo" (probe) só com os filtros
     * recebidos. Os campos não informados ficam null e o matcher os ignora.
     */
    public Page<GymClass> findByExample(Modality modality,
                                        Level level,
                                        String instructor,
                                        Pageable pageable) {

        GymClass probe = GymClass.builder()
                .modality(modality)
                .level(level)
                .instructor(instructor)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        return repository.findAll(Example.of(probe, matcher), pageable);
    }

    /** Specification: a busca que o Example não faz — faixas, "contém" e vagas. */
    public Page<GymClass> search(String title,
                                 Modality modality,
                                 Level level,
                                 LocalDateTime startsAfter,
                                 LocalDateTime startsBefore,
                                 Boolean availableOnly,
                                 Pageable pageable) {

        return repository.findAll(
                GymClassSpecifications.search(title, modality, level, startsAfter, startsBefore, availableOnly),
                pageable);
    }

    public GymClass findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
    }

    public GymClass create(GymClassRequest request) {
        return repository.save(request.toEntity());
    }

    public GymClass update(Long id, GymClassRequest request) {
        findById(id);
        GymClass gymClass = request.toEntity();
        gymClass.setId(id);
        return repository.save(gymClass);
    }

    // apaga as inscrições antes, senão a chave estrangeira impede o delete
    @Transactional
    public void delete(Long id) {
        GymClass gymClass = findById(id);
        enrollmentRepository.deleteByGymClassId(id);
        repository.delete(gymClass);
    }

    /** Vagas restantes: capacidade menos as inscrições já feitas. */
    public int availableSpots(GymClass gymClass) {
        long enrolled = enrollmentRepository.countByGymClassId(gymClass.getId());
        return gymClass.getCapacity() - (int) enrolled;
    }
}
