package com.fiap.techchallenge4.useCase.entrega;

import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;

public interface EntregaUseCase {

    void prepara(final PreparaEntregaDTO evento);
}
