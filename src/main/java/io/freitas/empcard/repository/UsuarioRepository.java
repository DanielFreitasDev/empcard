package io.freitas.empcard.repository;

import io.freitas.empcard.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByNomeUsuarioIgnoreCase(String nomeUsuario);

    boolean existsByNomeUsuarioIgnoreCase(String nomeUsuario);
}
