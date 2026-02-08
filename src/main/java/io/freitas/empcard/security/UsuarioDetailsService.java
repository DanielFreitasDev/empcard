package io.freitas.empcard.security;

import io.freitas.empcard.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementacao de autenticacao baseada em usuarios persistidos no banco.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Busca usuario por login para autenticacao.
     *
     * @param username nome de usuario informado na tela de login
     * @return detalhes do usuario para o Spring Security
     * @throws UsernameNotFoundException quando usuario nao existe
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Tentando autenticar usuario: {}", username);

        return usuarioRepository.findByNomeUsuarioIgnoreCase(username)
                .map(UsuarioAutenticado::new)
                .orElseThrow(() -> {
                    log.warn("Usuario nao encontrado para autenticacao: {}", username);
                    return new UsernameNotFoundException("Usuario nao encontrado");
                });
    }
}
