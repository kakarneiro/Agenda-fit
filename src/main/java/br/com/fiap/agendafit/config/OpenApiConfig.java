package br.com.fiap.agendafit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Título, versão e descrição que aparecem no topo do Swagger UI.
 *
 * O esquema "bearer-jwt" faz o botão Authorize aparecer: dá para colar o token
 * do /auth/login e testar as rotas protegidas direto pela interface.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI agendaFitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agenda Fit API")
                        .version("1.0.0")
                        .description("""
                                API da agenda de aulas de uma academia.

                                O administrador cadastra as aulas; o aluno consulta a grade, \
                                se inscreve e gerencia apenas as próprias inscrições.

                                As rotas protegidas usam JWT: faça login em /auth/login e \
                                informe o token no botão Authorize."""))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME));
    }
}
