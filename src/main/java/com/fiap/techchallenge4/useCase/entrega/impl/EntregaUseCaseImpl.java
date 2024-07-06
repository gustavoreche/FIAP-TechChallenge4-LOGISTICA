package com.fiap.techchallenge4.useCase.entrega.impl;

import com.fiap.techchallenge4.domain.Entrega;
import com.fiap.techchallenge4.domain.IdPedido;
import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.domain.StatusEntregaEnum;
import com.fiap.techchallenge4.infrasctructure.consumer.response.CancelaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.controller.dto.AtualizaClienteDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.controller.dto.AtualizaPedidoDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import com.fiap.techchallenge4.useCase.entrega.EntregaUseCase;
import com.fiap.techchallenge4.useCase.entregador.EntregadorUseCase;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EntregaUseCaseImpl implements EntregaUseCase {

    private final EntregaRepository repository;
    private final EntregadorUseCase entregadorService;
    private final StreamBridge streamBridge;

    public EntregaUseCaseImpl(final EntregaRepository repository,
                              final EntregadorUseCase entregadorService,
                              final StreamBridge streamBridge) {
        this.repository = repository;
        this.entregadorService = entregadorService;
        this.streamBridge = streamBridge;
    }

    @Override
    public void prepara(PreparaEntregaDTO evento) {
        final var entrega = new Entrega(
                evento.idDoPedido(),
                evento.cpfCliente(),
                evento.ean(),
                evento.quantidade()
        );

        final var entregaNaBaseDeDados = this.repository.findById(entrega.idDoPedido());
        if(entregaNaBaseDeDados.isPresent()) {
            System.out.println("Entrega já esta sendo preparada");
            return;
        }

        final var entregaEntity = EntregaEntity.builder()
                        .idDoPedido(entrega.idDoPedido())
                        .cpfCliente(entrega.cpfCliente())
                        .ean(entrega.ean())
                        .quantidadeDoProduto(entrega.quantidadeDoProduto())
                        .cpfEntregador(this.entregadorService.escolhe())
                        .statusEntrega(StatusEntregaEnum.CRIADO)
                        .dataDeCriacao(LocalDateTime.now())
                        .build();

        this.repository.save(entregaEntity);

    }

    @Override
    public boolean atualiza(final Long idDoPedido,
                            final StatusEntregaControllerEnum statusEntrega) {
        final var idDoPedidoObjeto = new IdPedido(idDoPedido);

        try {
            final var status = statusEntrega.equals(StatusEntregaControllerEnum.EM_TRANSPORTE)
                    ? StatusEntregaEnum.CRIADO
                    : statusEntrega.equals(StatusEntregaControllerEnum.ENTREGUE)
                    ? StatusEntregaEnum.EM_TRANSPORTE
                    : null;
            final var entregaNaBase = this.repository.findByIdDoPedidoAndStatusEntrega(idDoPedidoObjeto.numero(), status);
            if(entregaNaBase.isEmpty()) {
                System.out.println("Entrega não está cadastrada ou está com outro STATUS");
                return false;
            }

            final var entrega = entregaNaBase.get();

            if(statusEntrega.equals(StatusEntregaControllerEnum.EM_TRANSPORTE)) {
                final var informacoesDoEndereco = this.entregadorService
                        .pegaInformacoesDoEndereco(entrega.getCpfCliente(), entrega.getCpfEntregador());

                final var entregaEntity = EntregaEntity.builder()
                        .idDoPedido(idDoPedidoObjeto.numero())
                        .cpfCliente(entrega.getCpfCliente())
                        .ean(entrega.getEan())
                        .quantidadeDoProduto(entrega.getQuantidadeDoProduto())
                        .cpfEntregador(entrega.getCpfEntregador())
                        .tempoEstimadoDeEntregaEmHoras(informacoesDoEndereco.tempoEstimadoDeEntregaEmHoras())
                        .enderecoDeEntrega(informacoesDoEndereco.enderecoDeEntrega())
                        .statusEntrega(StatusEntregaEnum.EM_TRANSPORTE)
                        .dataDeCriacao(LocalDateTime.now())
                        .build();
                this.repository.save(entregaEntity);
            }
            else if(statusEntrega.equals(StatusEntregaControllerEnum.ENTREGUE)) {
                this.entregadorService.defineEntregadorComoDisponivel(entrega.getCpfEntregador());

                final var entregaEntity = EntregaEntity.builder()
                        .idDoPedido(idDoPedidoObjeto.numero())
                        .cpfCliente(entrega.getCpfCliente())
                        .ean(entrega.getEan())
                        .quantidadeDoProduto(entrega.getQuantidadeDoProduto())
                        .cpfEntregador(entrega.getCpfEntregador())
                        .tempoEstimadoDeEntregaEmHoras(entrega.getTempoEstimadoDeEntregaEmHoras())
                        .enderecoDeEntrega(entrega.getEnderecoDeEntrega())
                        .statusEntrega(StatusEntregaEnum.ENTREGUE)
                        .dataDeCriacao(LocalDateTime.now())
                        .build();
                this.repository.save(entregaEntity);
            }

            this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                    entrega.getCpfCliente(),
                    statusEntrega));

            this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                    idDoPedidoObjeto.numero(),
                    statusEntrega));

            return true;
        } catch (Exception e) {
            System.err.println("Error= " + e);
            return false;
        }

    }

    @Override
    public void cancela(CancelaEntregaDTO evento) {
        System.err.println("OLA");
        final var idDoPedidoObjeto = new IdPedido(evento.idDoPedido());

        final var entregaNaBase = this.repository.findByIdDoPedidoAndStatusEntrega(idDoPedidoObjeto.numero(), StatusEntregaEnum.CRIADO);
        if(entregaNaBase.isPresent()) {
            final var entrega = entregaNaBase.get();
            final var entregaEntity = EntregaEntity.builder()
                    .idDoPedido(entrega.getIdDoPedido())
                    .cpfCliente(entrega.getCpfCliente())
                    .ean(entrega.getEan())
                    .quantidadeDoProduto(entrega.getQuantidadeDoProduto())
                    .cpfEntregador(entrega.getCpfEntregador())
                    .statusEntrega(StatusEntregaEnum.CANCELADO)
                    .dataDeCriacao(LocalDateTime.now())
                    .build();

            this.repository.save(entregaEntity);
            return;
        }
        System.out.println("Entrega já esta sendo preparada ou ja foi cancelada");

    }

}
