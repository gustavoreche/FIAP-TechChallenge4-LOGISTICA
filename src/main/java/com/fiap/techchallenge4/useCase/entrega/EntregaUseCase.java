package com.fiap.techchallenge4.useCase.entrega;

import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.infrasctructure.consumer.response.CancelaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;

public interface EntregaUseCase {

    void prepara(final PreparaEntregaDTO evento);

    boolean atualiza(final Long idDoPedido,
                     final StatusEntregaControllerEnum statusEntrega);

    void cancela(final CancelaEntregaDTO evento);
}
