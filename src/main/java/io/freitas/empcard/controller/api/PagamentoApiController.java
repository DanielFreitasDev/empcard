package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.PagamentoFormDto;
import io.freitas.empcard.dto.PagamentoResponseDto;
import io.freitas.empcard.mapper.PagamentoMapper;
import io.freitas.empcard.service.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST para operacoes de pagamentos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "Gerenciamento de pagamentos")
public class PagamentoApiController {

    private final PagamentoService pagamentoService;

    /**
     * Lista pagamentos cadastrados.
     *
     * @return lista de pagamentos
     */
    @GetMapping
    @Operation(summary = "Listar pagamentos")
    public List<PagamentoResponseDto> listar() {
        return pagamentoService.listarTodos().stream().map(PagamentoMapper::paraResponse).toList();
    }

    /**
     * Busca pagamento por id.
     *
     * @param id id do pagamento
     * @return pagamento encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por id")
    public PagamentoResponseDto buscarPorId(@PathVariable Long id) {
        return PagamentoMapper.paraResponse(pagamentoService.buscarPorId(id));
    }

    /**
     * Cria pagamento.
     *
     * @param form dados de entrada
     * @return pagamento criado
     */
    @PostMapping
    @Operation(summary = "Criar pagamento")
    public ResponseEntity<PagamentoResponseDto> criar(@Valid @RequestBody PagamentoFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PagamentoMapper.paraResponse(pagamentoService.criar(form)));
    }

    /**
     * Atualiza pagamento.
     *
     * @param id   id do pagamento
     * @param form dados atualizados
     * @return pagamento atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pagamento")
    public PagamentoResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody PagamentoFormDto form) {
        return PagamentoMapper.paraResponse(pagamentoService.atualizar(id, form));
    }

    /**
     * Exclui pagamento.
     *
     * @param id id do pagamento
     * @return resposta sem conteudo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pagamento")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pagamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
