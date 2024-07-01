package com.fiap.techchallenge4.infrasctructure.entregador.repository;

import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntregadorRepository extends JpaRepository<EntregadorEntity, String> {

    List<EntregadorEntity> findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(final boolean estaEmEntrega);

}
