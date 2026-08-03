package com.algz.alms.repositorios;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.Usuario;
import com.algz.alms.enumeraciones.Rol;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID>{

    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);

    boolean existsByRol(Rol rol);
}
