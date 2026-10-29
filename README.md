# Agenda Fit API

API REST da agenda de aulas de uma academia. O administrador cadastra as aulas;
o aluno consulta a grade, se inscreve e gerencia apenas as próprias inscrições.

Projeto Diamante — Java Advanced (FIAP).

## Integrantes

| Nome | RM |
|---|---|
| Matheus Carneiro Maciel | 567753 |
| Murilo Marques | 568224 |
| Paulo Henrique da Silva Kian | 563343 |

## Tecnologias

- Java 25 e Spring Boot 4.1.1
- Spring Web, Spring Data JPA e Bean Validation
- Spring Security + OAuth2 Resource Server (JWT HS256)
- Spring HATEOAS
- springdoc-openapi 3.1.1 (Swagger UI)
- Banco H2 em memória

## Como executar

O projeto sobe sem nenhuma configuração extra.

```bash
mvn spring-boot:run
```

Ou abra o projeto na IDE e execute a classe `AgendaFitApplication`.

A API fica em `http://localhost:8080` e o Swagger UI em `http://localhost:8080/swagger-ui.html`.

O banco é em memória e já sobe com 60 aulas e 3 usuários, recriados a cada inicialização:

| E-mail | Senha | Papel |
|---|---|---|
| admin@fit.com | admin123 | ADMIN |
| ana@fit.com | ana123 | USER |
| bruno@fit.com | bruno123 | USER |

As aulas de capacidade 2 já começam lotadas, o que facilita testar o `availableOnly` e o 409.

## Autenticação

O login devolve um JWT assinado em HS256. Envie-o em todas as rotas protegidas:

```
Authorization: Bearer <token>
```

No Swagger UI, use o botão **Authorize** e cole apenas o token.

### Cadastro

`POST /auth/register` → **201** · **400** · **409** (e-mail em uso)

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Carla Dias","email":"carla@fit.com","password":"carla123"}'
```

Resposta (a senha nunca sai da API):

```json
{ "id": 4, "name": "Carla Dias", "email": "carla@fit.com", "role": "USER" }
```

### Login

`POST /auth/login` → **200** · **401**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fit.com","password":"admin123"}'
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "expiresIn": 3600 }
```

## Endpoints

| Método | Rota | Acesso |
|---|---|---|
| POST | `/auth/register` | livre |
| POST | `/auth/login` | livre |
| GET | `/classes`, `/classes/example`, `/classes/search` | livre |
| GET | `/classes/{id}` | livre |
| POST | `/classes` | ADMIN |
| PUT | `/classes/{id}` | ADMIN |
| DELETE | `/classes/{id}` | ADMIN |
| POST | `/classes/{id}/enrollments` | logado |
| GET | `/enrollments/me` | logado |
| DELETE | `/enrollments/{id}` | dono ou ADMIN |

### Listagem paginada

`GET /classes` → **200**

Sem parâmetros devolve a página 0 com 10 aulas, ordenadas por `startsAt`.

```bash
curl "http://localhost:8080/classes"
curl "http://localhost:8080/classes?page=1&size=5"
```

### Busca por Query by Example

`GET /classes/example` → **200**

Filtros exatos e opcionais: `modality`, `level` e `instructor`.
Textos ignoram maiúsculas e minúsculas; filtros não informados são ignorados.

```bash
curl "http://localhost:8080/classes/example?modality=YOGA&level=BEGINNER&instructor=carla%20mendes"
```

### Busca por Specification

`GET /classes/search` → **200**

A busca que o Example não faz: faixas de horário, "contém" e disponibilidade.

| Parâmetro | Regra |
|---|---|
| `title` | contém, sem caixa |
| `modality` | igual |
| `level` | igual |
| `startsAfter` | `startsAt` ≥ valor |
| `startsBefore` | `startsAt` ≤ valor |
| `availableOnly` | `true`: inscritos < `capacity` |

Todos são opcionais e combinam entre si.

```bash
curl "http://localhost:8080/classes/search?title=yoga&availableOnly=true"
curl "http://localhost:8080/classes/search?startsAfter=2026-11-03T00:00:00&startsBefore=2026-11-05T23:59:59"
```

### Detalhe de uma aula

`GET /classes/{id}` → **200** · **404**

```bash
curl "http://localhost:8080/classes/7"
```

### Cadastro de aula (ADMIN)

`POST /classes` → **201** (com header `Location`) · **400** · **401** · **403**

```bash
curl -X POST http://localhost:8080/classes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Yoga ao amanhecer",
    "modality": "YOGA",
    "level": "BEGINNER",
    "instructor": "Carla Mendes",
    "startsAt": "2026-12-01T07:00:00",
    "durationMinutes": 60,
    "capacity": 20
  }'
```

### Atualização de aula (ADMIN)

`PUT /classes/{id}` → **200** · **400** · **401** · **403** · **404**

```bash
curl -X PUT http://localhost:8080/classes/7 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Yoga ao amanhecer (turma nova)",
    "modality": "YOGA",
    "level": "BEGINNER",
    "instructor": "Carla Mendes",
    "startsAt": "2026-12-01T07:30:00",
    "durationMinutes": 60,
    "capacity": 25
  }'
```

### Remoção de aula (ADMIN)

`DELETE /classes/{id}` → **204** · **401** · **403** · **404**

```bash
curl -X DELETE http://localhost:8080/classes/7 -H "Authorization: Bearer $TOKEN"
```

### Inscrição

`POST /classes/{id}/enrollments` → **201** · **401** · **404** · **409**

Não tem corpo: o aluno é identificado pelo token.

```bash
curl -X POST http://localhost:8080/classes/7/enrollments -H "Authorization: Bearer $TOKEN"
```

O 409 acontece quando a aula está lotada ou quando o aluno já está inscrito nela.

### Minhas inscrições

`GET /enrollments/me` → **200** · **401**

```bash
curl "http://localhost:8080/enrollments/me" -H "Authorization: Bearer $TOKEN"
```

### Cancelar inscrição

`DELETE /enrollments/{id}` → **204** · **401** · **403** · **404**

O aluno cancela apenas as próprias inscrições; o ADMIN cancela qualquer uma.

```bash
curl -X DELETE http://localhost:8080/enrollments/3 -H "Authorization: Bearer $TOKEN"
```

## Formatos de resposta

### Uma aula (EntityModel)

```json
{
  "id": 7,
  "title": "Yoga ao amanhecer",
  "modality": "YOGA",
  "level": "BEGINNER",
  "instructor": "Carla Mendes",
  "startsAt": "2026-11-03T07:00:00",
  "durationMinutes": 60,
  "capacity": 20,
  "availableSpots": 18,
  "_links": {
    "self": { "href": "http://localhost:8080/classes/7" },
    "classes": { "href": "http://localhost:8080/classes" },
    "enroll": { "href": "http://localhost:8080/classes/7/enrollments" }
  }
}
```

### Uma inscrição

```json
{
  "id": 3,
  "classId": 7,
  "classTitle": "Yoga ao amanhecer",
  "startsAt": "2026-11-03T07:00:00",
  "createdAt": "2026-10-29T14:22:10",
  "_links": {
    "class": { "href": "http://localhost:8080/classes/7" },
    "cancel": { "href": "http://localhost:8080/enrollments/3" }
  }
}
```

### Uma página (PagedModel)

```json
{
  "_embedded": { "classes": [ ... ] },
  "_links": { "first": {}, "prev": {}, "self": {}, "next": {}, "last": {} },
  "page": { "size": 10, "totalElements": 60, "totalPages": 6, "number": 0 }
}
```

Os links de navegação preservam os filtros da busca. Uma página vazia não traz `_embedded`.

### Erros

Todos os erros saem no mesmo formato, inclusive os 401 e 403 vindos da camada de segurança:

```json
{ "error": "Class not found" }
```

| Situação | Status |
|---|---|
| Dados de entrada inválidos ou JSON malformado | 400 |
| Sem token ou token inválido | 401 |
| Sem permissão para a operação | 403 |
| Aula ou inscrição inexistente | 404 |
| E-mail em uso, aula lotada ou inscrição duplicada | 409 |

## Links condicionais

O servidor diz o que cada usuário pode fazer: o app só mostra um botão quando o
link correspondente vem na resposta. O assembler consulta o `SecurityContext` a
cada requisição.

| Recurso | Link | Aparece quando |
|---|---|---|
| Aula | `enroll` | logado, com vaga e ainda não inscrito |
| Aula | `update` | usuário ADMIN |
| Aula | `delete` | usuário ADMIN |
| Página de aulas | `create` | usuário ADMIN |
| Inscrição | `cancel` | dono da inscrição ou ADMIN |

Na prática: sem token, uma aula traz apenas `self` e `classes`; logado como aluno,
ganha `enroll`; logado como ADMIN, ganha `update` e `delete`.

## Decisões de implementação

**A claim `role` vira `ROLE_*` por um JwtAuthenticationConverter.** Por padrão o
Spring Security leria a claim `scope` e criaria authorities `SCOPE_*`. Como o
contrato define a claim `role` e a avaliação pede `hasRole("ADMIN")`, a
`SecurityConfig` registra um converter que lê `role` e aplica o prefixo `ROLE_`.

**DTO de resposta para a aula, em vez da entidade.** O contrato exige
`availableSpots`, que é calculado a partir das inscrições e não é coluna da
tabela. Por isso existe o `GymClassResponse`, que também mantém a estrutura de
persistência longe de quem consome a API.

**401 e 403 tratados na camada de segurança.** Esses dois erros acontecem nos
filtros, antes de qualquer controller, então o `GlobalExceptionHandler` nunca os
veria. A `SecurityConfig` registra um `AuthenticationEntryPoint` e um
`AccessDeniedHandler` que escrevem o mesmo `{"error": "..."}` dos demais erros.

**Specification combinada a partir de uma condição neutra.** Cada filtro do
`/search` é uma Specification isolada; o método `search()` combina com `and`
apenas os que vieram preenchidos, partindo de uma `conjunction()`. Assim uma
busca sem nenhum filtro devolve todas as aulas em vez de quebrar. O
`availableOnly` usa uma subquery que conta as inscrições da aula e compara com a
capacidade.

**`@Relation(collectionRelation = ...)` nos DTOs de resposta.** Sem a anotação o
Spring HATEOAS nomearia as coleções como `_embedded.gymClassResponseList` e
`_embedded.enrollmentResponseList`, fora do contrato esperado pelo app.

**A regra de dono fica no service, não só no link.** O link `cancel` some para
quem não pode cancelar, mas o `EnrollmentService` verifica de novo antes de
apagar — esconder o botão não é controle de acesso.
