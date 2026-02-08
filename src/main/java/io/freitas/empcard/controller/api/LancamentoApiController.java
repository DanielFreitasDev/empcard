package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.LancamentoFormDto;
import io.freitas.empcard.dto.LancamentoResponseDto;
import io.freitas.empcard.mapper.LancamentoMapper;
import io.freitas.empcard.service.LancamentoService;
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
 * API REST para operacoes de lancamentos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lancamentos")
@Tag(name = "Lancamentos", description = "Gerenciamento de lancamentos")
public class LancamentoApiController {

    private final LancamentoService lancamentoService;

    /**
     * Lista lancamentos cadastrados.
     *
     * @return lista de lancamentos
     */
    @GetMapping
    @Operation(summary = "Listar lancamentos")
    public List<LancamentoResponseDto> listar() {
        return lancamentoService.listarTodos().stream().map(LancamentoMapper::paraResponse).toList();
    }

    /**
     * Busca lancamento por id.
     *
     * @param id id do lancamento
     * @return lancamento encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar lancamento por id")
    public LancamentoResponseDto buscarPorId(@PathVariable Long id) {
        return LancamentoMapper.paraResponse(lancamentoService.buscarPorId(id));
    }

    /**
     * Cria lancamento.
     *
     * @param form dados de entrada
     * @return lancamento criado
     */
    @PostMapping
    @Operation(summary = "Criar lancamento")
    public ResponseEntity<LancamentoResponseDto> criar(@Valid @RequestBody LancamentoFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LancamentoMapper.paraResponse(lancamentoService.criar(form)));
    }

    /**
     * Atualiza lancamento.
     *
     * @param id   id do lancamento
     * @param form dados atualizados
     * @return lancamento atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar lancamento")
    public LancamentoResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody LancamentoFormDto form) {
        return LancamentoMapper.paraResponse(lancamentoService.atualizar(id, form));
    }

    /**
     * Alterna status de lancamento.
     *
     * @param id id do lancamento
     * @return resposta sem conteudo
     */
    @PatchMapping("/{id}/ativo")
    @Operation(summary = "Alternar status de lancamento")
    public ResponseEntity<Void> alternarAtivo(@PathVariable Long id) {
        lancamentoService.alternarAtivo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui lancamento.
     *
     * @param id id do lancamento
     * @return resposta sem conteudo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lancamento")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        lancamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
