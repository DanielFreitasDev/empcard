package io.freitas.empcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configuracoes de logging HTTP para ampliar rastreabilidade operacional.
 */
@Configuration
public class LogConfig {

    /**
     * Habilita log detalhado de requisicoes recebidas (metodo, URL e query string).
     *
     * @return filtro de logging de requests
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST RECEBIDA: ");
        return filter;
    }
}
