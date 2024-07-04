package com.fiap.techchallenge4.infrasctructure.entrega.repository;

import com.fiap.techchallenge4.domain.StatusEntregaEnum;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntregaRepository extends JpaRepository<EntregaEntity, Long> {
    Optional<EntregaEntity> findByIdDoPedidoAndStatusEntrega(final Long numero,
                                                             final StatusEntregaEnum statusEntregaEnum);
}
