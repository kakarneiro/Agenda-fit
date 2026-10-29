package br.com.fiap.agendafit.controller;

import java.time.LocalDateTime;

import br.com.fiap.agendafit.dto.GymClassRequest;
import br.com.fiap.agendafit.dto.GymClassResponse;
import br.com.fiap.agendafit.hateoas.GymClassModelAssembler;
import br.com.fiap.agendafit.model.GymClass;
import br.com.fiap.agendafit.model.Level;
import br.com.fiap.agendafit.model.Modality;
import br.com.fiap.agendafit.security.AuthenticatedUser;
import br.com.fiap.agendafit.service.GymClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "Aulas", description = "Grade de aulas da academia: listagem, buscas e gestão")
public class GymClassController {

    private final GymClassService service;
    private final GymClassModelAssembler assembler;
    private final PagedResourcesAssembler<GymClass> pagedAssembler;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping
    @Operation(summary = "Lista a grade de aulas de forma paginada, da mais próxima para a mais distante")
    public PagedModel<EntityModel<GymClassResponse>> findAll(
            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {

        return toPagedModel(service.findAll(pageable));
    }

    @GetMapping("/example")
    @Operation(summary = "Busca aulas iguais a uma aula de exemplo (Query by Example)")
    public PagedModel<EntityModel<GymClassResponse>> findByExample(
            @Parameter(description = "Modalidade: YOGA, PILATES, SPINNING, FUNCTIONAL ou DANCE")
            @RequestParam(required = false) Modality modality,

            @Parameter(description = "Nível: BEGINNER, INTERMEDIATE ou ADVANCED")
            @RequestParam(required = false) Level level,

            @Parameter(description = "Nome exato do instrutor, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String instructor,

            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {

        return toPagedModel(service.findByExample(modality, level, instructor, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Busca aulas por trecho do título, faixa de horário e disponibilidade (Specification)")
    public PagedModel<EntityModel<GymClassResponse>> search(
            @Parameter(description = "Trecho do título, sem diferenciar maiúsculas de minúsculas")
            @RequestParam(required = false) String title,

            @Parameter(description = "Modalidade exata")
            @RequestParam(required = false) Modality modality,

            @Parameter(description = "Nível exato")
            @RequestParam(required = false) Level level,

            @Parameter(description = "Apenas aulas que começam a partir deste momento (ex.: 2026-11-03T07:00:00)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAfter,

            @Parameter(description = "Apenas aulas que começam até este momento")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsBefore,

            @Parameter(description = "true devolve apenas aulas que ainda têm vaga")
            @RequestParam(required = false) Boolean availableOnly,

            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {

        return toPagedModel(
                service.search(title, modality, level, startsAfter, startsBefore, availableOnly, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma aula pelo id")
    @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    public EntityModel<GymClassResponse> findById(@PathVariable Long id) {
        return assembler.toModel(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova aula (apenas ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido"),
            @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN")
    })
    public ResponseEntity<EntityModel<GymClassResponse>> create(@RequestBody @Valid GymClassRequest request) {
        EntityModel<GymClassResponse> model = assembler.toModel(service.create(request));

        // o Location reaproveita o link self que o assembler acabou de montar
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma aula existente (apenas ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido"),
            @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    public EntityModel<GymClassResponse> update(@PathVariable Long id,
                                                @RequestBody @Valid GymClassRequest request) {
        return assembler.toModel(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma aula e as inscrições dela (apenas ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Sem token ou token inválido"),
            @ApiResponse(responseCode = "403", description = "Usuário não é ADMIN"),
            @ApiResponse(responseCode = "404", description = "Aula não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converte a página e acrescenta o link "create", que só aparece para o ADMIN.
     * É o mesmo tratamento para a listagem e para as duas buscas.
     */
    private PagedModel<EntityModel<GymClassResponse>> toPagedModel(Page<GymClass> page) {
        PagedModel<EntityModel<GymClassResponse>> model = pagedAssembler.toModel(page, assembler);

        if (authenticatedUser.isAdmin()) {
            model.add(linkTo(methodOn(GymClassController.class).create(null)).withRel("create"));
        }

        return model;
    }
}
