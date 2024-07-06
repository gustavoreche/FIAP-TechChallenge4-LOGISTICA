package com.fiap.techchallenge4.infrasctructure.consumer;

import com.fiap.techchallenge4.infrasctructure.consumer.response.CancelaEntregaDTO;
import com.fiap.techchallenge4.useCase.entrega.EntregaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ConsumerCancelaEntrega {

    private final EntregaUseCase service;

    public ConsumerCancelaEntrega(final EntregaUseCase service) {
        this.service = service;
    }

    @Bean
    public Consumer<CancelaEntregaDTO> cancela() {
        return evento -> {
            this.service.cancela(evento);
            System.out.println("Evento consumido com sucesso!");
        };
    }


}
