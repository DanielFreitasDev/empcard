package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.PessoaFormDto;
import io.freitas.empcard.dto.PessoaResponseDto;
import io.freitas.empcard.mapper.PessoaMapper;
import io.freitas.empcard.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST para operacoes de pessoas.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pessoas")
@Tag(name = "Pessoas", description = "Gerenciamento de pessoas")
public class PessoaApiController {

    private final PessoaService pessoaService;

    /**
     * Lista todas as pessoas cadastradas.
     *
     * @return lista de pessoas
     */
    @GetMapping
    @Operation(summary = "Listar pessoas")
    public List<PessoaResponseDto> listar() {
        return pessoaService.listarTodos().stream().map(PessoaMapper::paraResponse).toList();
    }

    /**
     * Busca pessoa por id.
     *
     * @param id id da pessoa
     * @return pessoa encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pessoa por id")
    public PessoaResponseDto buscarPorId(@PathVariable Long id) {
        return PessoaMapper.paraResponse(pessoaService.buscarPorId(id));
    }

    /**
     * Cria nova pessoa.
     *
     * @param form dados de entrada
     * @return pessoa criada
     */
    @PostMapping
    @Operation(summary = "Criar pessoa")
    public ResponseEntity<PessoaResponseDto> criar(@Valid @RequestBody PessoaFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PessoaMapper.paraResponse(pessoaService.criar(form)));
    }

    /**
     * Atualiza pessoa existente.
     *
     * @param id   id da pessoa
     * @param form dados atualizados
     * @return pessoa atualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pessoa")
    public PessoaResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody PessoaFormDto form) {
        return PessoaMapper.paraResponse(pessoaService.atualizar(id, form));
    }

    /**
     * Alterna status ativo/inativo.
     *
     * @param id id da pessoa
     * @return sem conteudo
     */
    @PatchMapping("/{id}/ativo")
    @Operation(summary = "Alternar status de pessoa")
    public ResponseEntity<Void> alternarAtivo(@PathVariable Long id) {
        pessoaService.alternarAtivo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui pessoa quando permitido.
     *
     * @param id id da pessoa
     * @return sem conteudo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pessoa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pessoaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
