package com.algz.alms.repositorios;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.algz.alms.entidades.InvitacionRegistro;

import jakarta.persistence.LockModeType;

@Repository
public interface InvitacionRegistroRepositorio extends JpaRepository<InvitacionRegistro, UUID>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InvitacionRegistro i where i.tokenHash = :tokenHash")
    Optional<InvitacionRegistro> findByTokenHashParaActualizar(@Param("tokenHash") String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InvitacionRegistro i where i.invitacionId = :invitacionId")
    Optional<InvitacionRegistro> findByIdParaActualizar(@Param("invitacionId") UUID invitacionId);
}
