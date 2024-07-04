package com.fiap.techchallenge4.useCase.entregador;

import com.fiap.techchallenge4.domain.InformacoesDoEndereco;

public interface EntregadorUseCase {

    String escolhe();

    InformacoesDoEndereco pegaInformacoesDoEndereco(final String cpfCliente,
                                                    final String cpfEntregador);

    void defineEntregadorComoDisponivel(final String cpfEntregador);
}
