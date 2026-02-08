package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.ResetSenhaUsuarioDto;
import io.freitas.empcard.dto.UsuarioFormDto;
import io.freitas.empcard.dto.UsuarioResponseDto;
import io.freitas.empcard.mapper.UsuarioMapper;
import io.freitas.empcard.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST para administracao de usuarios.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gerenciamento de usuarios")
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    /**
     * Lista usuarios do sistema.
     *
     * @return lista de usuarios
     */
    @GetMapping
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponseDto> listar() {
        return usuarioService.listarTodos().stream().map(UsuarioMapper::paraResponse).toList();
    }

    /**
     * Cria usuario.
     *
     * @param form dados de cadastro
     * @return usuario criado
     */
    @PostMapping
    @Operation(summary = "Criar usuario")
    public ResponseEntity<UsuarioResponseDto> criar(@Valid @RequestBody UsuarioFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.paraResponse(usuarioService.criarUsuario(form)));
    }

    /**
     * Alterna status ativo de usuario.
     *
     * @param id id do usuario
     * @return resposta sem conteudo
     */
    @PatchMapping("/{id}/ativo")
    @Operation(summary = "Alternar status de usuario")
    public ResponseEntity<Void> alternarAtivo(@PathVariable Long id) {
        usuarioService.alternarAtivo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Redefine senha do usuario por acao administrativa.
     *
     * @param id   id do usuario
     * @param form nova senha
     * @return resposta sem conteudo
     */
    @PatchMapping("/{id}/senha")
    @Operation(summary = "Redefinir senha de usuario")
    public ResponseEntity<Void> redefinirSenha(@PathVariable Long id, @Valid @RequestBody ResetSenhaUsuarioDto form) {
        usuarioService.redefinirSenha(id, form.getNovaSenha());
        return ResponseEntity.noContent().build();
    }
}
