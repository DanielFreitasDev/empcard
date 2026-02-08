package io.freitas.empcard.service;

import io.freitas.empcard.dto.AlterarSenhaFormDto;
import io.freitas.empcard.dto.SetupInicialFormDto;
import io.freitas.empcard.dto.UsuarioFormDto;
import io.freitas.empcard.exception.RecursoNaoEncontradoException;
import io.freitas.empcard.exception.RegraDeNegocioException;
import io.freitas.empcard.model.PapelUsuario;
import io.freitas.empcard.model.Usuario;
import io.freitas.empcard.repository.UsuarioRepository;
import io.freitas.empcard.util.TextoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Regras de negocio para administracao de usuarios do sistema.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Informa se ja existe ao menos um usuario para decidir fluxo de setup inicial.
     *
     * @return true quando ha usuarios cadastrados
     */
    @Transactional(readOnly = true)
    public boolean possuiUsuarios() {
        boolean possui = usuarioRepository.count() > 0;
        log.info("Verificacao de usuarios existentes: {}", possui);
        return possui;
    }

    /**
     * Lista todos os usuarios para administracao.
     *
     * @return lista ordenada por nome de exibicao
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        log.info("Listando usuarios");
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNomeExibicao, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * Busca usuario por id com validacao.
     *
     * @param id identificador
     * @return usuario encontrado
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        log.info("Buscando usuario por id={}", id);
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para id " + id));
    }

    /**
     * Cria primeiro usuario administrador durante setup inicial.
     *
     * @param form dados de cadastro inicial
     * @return usuario administrador criado
     */
    @Transactional
    public Usuario criarPrimeiroAdmin(SetupInicialFormDto form) {
        if (possuiUsuarios()) {
            log.warn("Tentativa de executar setup inicial com usuarios ja existentes");
            throw new RegraDeNegocioException("O setup inicial ja foi concluido");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeExibicao(TextoUtils.normalizarMaiusculo(form.getNomeExibicao()));
        usuario.setNomeUsuario(TextoUtils.normalizarMaiusculo(form.getNomeUsuario()));
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setPapel(PapelUsuario.ADMIN);
        usuario.setAtivo(true);

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Primeiro administrador criado com sucesso. id={}, nomeUsuario={}", salvo.getId(), salvo.getNomeUsuario());
        return salvo;
    }

    /**
     * Cria novo usuario administrativo ou de consulta.
     *
     * @param form dados de cadastro
     * @return usuario criado
     */
    @Transactional
    public Usuario criarUsuario(UsuarioFormDto form) {
        String nomeUsuarioNormalizado = TextoUtils.normalizarMaiusculo(form.getNomeUsuario());

        if (usuarioRepository.existsByNomeUsuarioIgnoreCase(nomeUsuarioNormalizado)) {
            throw new RegraDeNegocioException("Ja existe usuario com este nome de login");
        }

        Usuario usuario = new Usuario();
        usuario.setNomeExibicao(TextoUtils.normalizarMaiusculo(form.getNomeExibicao()));
        usuario.setNomeUsuario(nomeUsuarioNormalizado);
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setPapel(form.getPapel());
        usuario.setAtivo(form.isAtivo());

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuario criado com sucesso. id={}, papel={}", salvo.getId(), salvo.getPapel());
        return salvo;
    }

    /**
     * Alterna status ativo/inativo de usuario com protecao para nao desativar ultimo admin.
     *
     * @param id identificador do usuario
     */
    @Transactional
    public void alternarAtivo(Long id) {
        Usuario usuario = buscarPorId(id);

        if (usuario.getPapel() == PapelUsuario.ADMIN && usuario.isAtivo() && contarAdminsAtivos() <= 1) {
            throw new RegraDeNegocioException("Nao e permitido desativar o ultimo administrador ativo");
        }

        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
        log.info("Status do usuario alterado. id={}, ativo={}", id, usuario.isAtivo());
    }

    /**
     * Atualiza senha do proprio usuario autenticado exigindo senha atual correta.
     *
     * @param nomeUsuario nome de usuario autenticado
     * @param form        formulario com senha atual e nova senha
     */
    @Transactional
    public void alterarSenhaPropria(String nomeUsuario, AlterarSenhaFormDto form) {
        Usuario usuario = usuarioRepository.findByNomeUsuarioIgnoreCase(nomeUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado"));

        if (!passwordEncoder.matches(form.getSenhaAtual(), usuario.getSenha())) {
            throw new RegraDeNegocioException("Senha atual informada nao confere");
        }

        usuario.setSenha(passwordEncoder.encode(form.getNovaSenha()));
        usuarioRepository.save(usuario);
        log.info("Senha alterada com sucesso para usuario={}", usuario.getNomeUsuario());
    }

    /**
     * Redefine senha de um usuario por acao administrativa.
     *
     * @param idUsuario id do usuario alvo
     * @param novaSenha nova senha em texto puro
     */
    @Transactional
    public void redefinirSenha(Long idUsuario, String novaSenha) {
        Usuario usuario = buscarPorId(idUsuario);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        log.info("Senha redefinida por administrador. usuarioId={}", idUsuario);
    }

    /**
     * Conta quantos administradores ativos existem no sistema.
     *
     * @return quantidade de admins ativos
     */
    @Transactional(readOnly = true)
    public long contarAdminsAtivos() {
        return usuarioRepository.findAll().stream()
                .filter(Usuario::isAtivo)
                .filter(usuario -> usuario.getPapel() == PapelUsuario.ADMIN)
                .count();
    }
}
