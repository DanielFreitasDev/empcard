package io.freitas.empcard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao da documentacao OpenAPI para os endpoints REST.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define metadados da API para exibicao no Swagger UI.
     *
     * @return instancia OpenAPI configurada
     */
    @Bean
    public OpenAPI empcardOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EmpCard API")
                        .description("API para gestao de emprestimos de cartao, cobrancas e pagamentos")
                        .version("v1")
                        .contact(new Contact().name("EmpCard").email("suporte@empcard.local"))
                        .license(new License().name("Uso privado")));
    }
}
