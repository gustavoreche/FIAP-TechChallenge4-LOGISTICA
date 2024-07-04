package com.fiap.techchallenge4.useCase.entregador.impl;

import com.fiap.techchallenge4.domain.InformacoesDoEndereco;
import com.fiap.techchallenge4.infrasctructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import com.fiap.techchallenge4.useCase.entregador.EntregadorUseCase;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class EntregadorUseCaseImpl implements EntregadorUseCase {

    private final EntregadorRepository repository;
    private final ClienteClient clientCliente;

    public EntregadorUseCaseImpl(final EntregadorRepository repository,
                                 final ClienteClient clientCliente) {
        this.repository = repository;
        this.clientCliente = clientCliente;
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

    @Override
    public InformacoesDoEndereco pegaInformacoesDoEndereco(final String cpfCliente,
                                                           final String cpfEntregador) {
        final var cliente = this.clientCliente.pegaCliente(cpfCliente);
        if(Objects.isNull(cliente)) {
            System.out.println("Cliente não encontrado");
            throw new RuntimeException("Cliente não encontrado");
        }

        final var entregadorNaBase = this.repository.findById(cpfEntregador);
        if(entregadorNaBase.isEmpty()) {
            System.out.println("Entregador não encontrado");
            throw new RuntimeException("Entregador não encontrado");
        }

        final var entregador = entregadorNaBase.get();

        final var entregadorEntity = EntregadorEntity.builder()
                .cpf(entregador.getCpf())
                .quantidadeDeEntregaRealizada(entregador.getQuantidadeDeEntregaRealizada())
                .estaEmEntrega(true)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        this.repository.save(entregadorEntity);

        return new InformacoesDoEndereco(
                cliente.enderecoSiglaEstado(),
                cliente.enderecoLogradouro().concat(" - N°").concat(String.valueOf(cliente.enderecoNumero()))
        );
    }

    @Override
    public void defineEntregadorComoDisponivel(final String cpfEntregador) {
        final var entregadorNaBase = this.repository.findById(cpfEntregador);
        if(entregadorNaBase.isEmpty()) {
            System.out.println("Entregador não encontrado");
            throw new RuntimeException("Entregador não encontrado");
        }

        final var entregador = entregadorNaBase.get();

        final var entregadorEntity = EntregadorEntity.builder()
                .cpf(entregador.getCpf())
                .quantidadeDeEntregaRealizada(entregador.getQuantidadeDeEntregaRealizada() + 1)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        this.repository.save(entregadorEntity);
    }

}
