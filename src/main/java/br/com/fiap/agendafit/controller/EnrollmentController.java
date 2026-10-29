package br.com.fiap.agendafit.controller;

import br.com.fiap.agendafit.dto.EnrollmentResponse;
import br.com.fiap.agendafit.hateoas.EnrollmentModelAssembler;
import br.com.fiap.agendafit.model.Enrollment;
import br.com.fiap.agendafit.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sem @RequestMapping na classe: a inscrição nasce dentro de uma aula
 * (/classes/{id}/enrollments), mas depois é um recurso próprio (/enrollments/{id}).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Inscrições", description = "Inscrição do aluno nas aulas e gestão das próprias inscrições")
public class EnrollmentController {

    private final EnrollmentService service;
    private final EnrollmentModelAssembler assembler;
    private final PagedResourcesAssembler<Enrollment> pagedAssembler;

    @PostMapping("/classes/{classId}/enrollments")
    @Operation(summary = "Inscreve o aluno logado na aula")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada"),
            @ApiResponse(responseCode = "409", description = "Aula lotada ou aluno já inscrito")
    })
    public ResponseEntity<EntityModel<EnrollmentResponse>> enroll(@PathVariable Long classId) {
        EntityModel<EnrollmentResponse> model = assembler.toModel(service.enroll(classId));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/enrollments/me")
    @Operation(summary = "Lista as inscrições do aluno logado, de forma paginada")
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido")
    public PagedModel<EntityModel<EnrollmentResponse>> findMine(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return pagedAssembler.toModel(service.findMine(pageable), assembler);
    }

    @DeleteMapping("/enrollments/{id}")
    @Operation(summary = "Cancela uma inscrição: o aluno só cancela as próprias, o ADMIN cancela qualquer uma")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido"),
            @ApiResponse(responseCode = "403", description = "A inscrição é de outro aluno"),
            @ApiResponse(responseCode = "404", description = "Inscrição não encontrada")
    })
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
