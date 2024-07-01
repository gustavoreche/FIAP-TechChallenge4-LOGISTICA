package com.fiap.techchallenge4.useCase.entregador.impl;

import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import com.fiap.techchallenge4.useCase.entregador.EntregadorUseCase;
import org.springframework.stereotype.Service;

@Service
public class EntregadorUseCaseImpl implements EntregadorUseCase {

    private final EntregadorRepository repository;

    public EntregadorUseCaseImpl(final EntregadorRepository repository) {
        this.repository = repository;
    }

    @Override
    public String escolhe() {
        final var entregadoresDisponiveis = this.repository
                .findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        if(entregadoresDisponiveis.isEmpty()) {
            throw new RuntimeException("Nenhum entregador disponível");
        }
        return entregadoresDisponiveis.get(0).getCpf();
    }

}
