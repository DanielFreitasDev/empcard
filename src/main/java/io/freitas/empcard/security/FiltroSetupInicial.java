package io.freitas.empcard.security;

import io.freitas.empcard.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Garante que o primeiro acesso direcione para criacao do primeiro admin quando nao ha usuarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FiltroSetupInicial extends OncePerRequestFilter {

    private static final Set<String> ROTAS_PERMITIDAS = Set.of(
            "/setup/inicial",
            "/setup/inicial/salvar",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/",
            "/favicon.ico",
            "/actuator/health",
            "/error"
    );

    private final UsuarioRepository usuarioRepository;

    /**
     * Intercepta todas as requisicoes para verificar se o sistema ja possui usuario cadastrado.
     *
     * @param request     requisicao atual
     * @param response    resposta HTTP
     * @param filterChain cadeia de filtros
     * @throws ServletException erro interno de servlet
     * @throws IOException      erro de IO
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long totalUsuarios = usuarioRepository.count();
        String path = request.getRequestURI();

        // Enquanto nao existir usuario, apenas a rota de setup e arquivos estaticos ficam liberados.
        if (totalUsuarios == 0 && !isRotaPermitida(path)) {
            log.info("Sistema sem usuarios. Redirecionando para setup inicial. path={}", path);
            response.sendRedirect(request.getContextPath() + "/setup/inicial");
            return;
        }

        // Se o setup foi concluido, bloquear acesso manual posterior a tela inicial de setup.
        if (totalUsuarios > 0 && path.startsWith("/setup/inicial")) {
            log.info("Tentativa de acesso ao setup inicial apos conclusao. path={}", path);
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verifica se a rota pode ser usada antes da criacao do primeiro usuario.
     *
     * @param path caminho solicitado
     * @return true quando a rota deve permanecer acessivel no setup inicial
     */
    private boolean isRotaPermitida(String path) {
        return ROTAS_PERMITIDAS.stream().anyMatch(path::startsWith);
    }
}
