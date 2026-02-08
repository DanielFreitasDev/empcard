package io.freitas.empcard.controller.api;

import io.freitas.empcard.dto.CartaoFormDto;
import io.freitas.empcard.dto.CartaoResponseDto;
import io.freitas.empcard.mapper.CartaoMapper;
import io.freitas.empcard.service.CartaoService;
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
 * API REST para operacoes de cartoes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cartoes")
@Tag(name = "Cartoes", description = "Gerenciamento de cartoes")
public class CartaoApiController {

    private final CartaoService cartaoService;

    /**
     * Lista cartoes cadastrados.
     *
     * @return lista de cartoes
     */
    @GetMapping
    @Operation(summary = "Listar cartoes")
    public List<CartaoResponseDto> listar() {
        return cartaoService.listarTodos().stream().map(CartaoMapper::paraResponse).toList();
    }

    /**
     * Busca cartao por id.
     *
     * @param id id do cartao
     * @return cartao encontrado
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartao por id")
    public CartaoResponseDto buscarPorId(@PathVariable Long id) {
        return CartaoMapper.paraResponse(cartaoService.buscarPorId(id));
    }

    /**
     * Cria cartao.
     *
     * @param form dados de entrada
     * @return cartao criado
     */
    @PostMapping
    @Operation(summary = "Criar cartao")
    public ResponseEntity<CartaoResponseDto> criar(@Valid @RequestBody CartaoFormDto form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CartaoMapper.paraResponse(cartaoService.criar(form)));
    }

    /**
     * Atualiza cartao.
     *
     * @param id   id do cartao
     * @param form dados atualizados
     * @return cartao atualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cartao")
    public CartaoResponseDto atualizar(@PathVariable Long id, @Valid @RequestBody CartaoFormDto form) {
        return CartaoMapper.paraResponse(cartaoService.atualizar(id, form));
    }

    /**
     * Alterna status de cartao.
     *
     * @param id id do cartao
     * @return resposta sem conteudo
     */
    @PatchMapping("/{id}/ativo")
    @Operation(summary = "Alternar status de cartao")
    public ResponseEntity<Void> alternarAtivo(@PathVariable Long id) {
        cartaoService.alternarAtivo(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Exclui cartao quando permitido.
     *
     * @param id id do cartao
     * @return resposta sem conteudo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cartao")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        cartaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
