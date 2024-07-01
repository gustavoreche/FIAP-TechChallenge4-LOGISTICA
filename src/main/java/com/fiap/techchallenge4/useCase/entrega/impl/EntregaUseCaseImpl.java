package com.fiap.techchallenge4.useCase.entrega.impl;

import com.fiap.techchallenge4.domain.Entrega;
import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import com.fiap.techchallenge4.useCase.entrega.EntregaUseCase;
import com.fiap.techchallenge4.useCase.entregador.EntregadorUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EntregaUseCaseImpl implements EntregaUseCase {

    private final EntregaRepository repository;
    private final EntregadorUseCase entregadorService;

    public EntregaUseCaseImpl(final EntregaRepository repository,
                              final EntregadorUseCase entregadorService) {
        this.repository = repository;
        this.entregadorService = entregadorService;
    }

    @Override
    public void prepara(PreparaEntregaDTO evento) {
        final var entrega = new Entrega(
                evento.idDoPedido(),
                evento.cpfCliente(),
                evento.ean(),
                evento.quantidade()
        );

        final var entregaNaBaseDeDados = this.repository.findById(entrega.getIdDoPedido());
        if(entregaNaBaseDeDados.isPresent()) {
            System.out.println("Entrega já esta sendo preparada");
            return;
        }

        final var entregaEntity = EntregaEntity.builder()
                        .cpfCliente(entrega.getCpfCliente())
                        .ean(entrega.getEan())
                        .quantidadeDoProduto(entrega.getQuantidadeDoProduto())
                        .cpfEntregador(this.entregadorService.escolhe())
                        .dataDeCriacao(LocalDateTime.now())
                        .build();

        this.repository.save(entregaEntity);

    }

}
