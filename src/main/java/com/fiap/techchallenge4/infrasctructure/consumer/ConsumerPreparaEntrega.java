package com.fiap.techchallenge4.infrasctructure.consumer;

import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;
import com.fiap.techchallenge4.useCase.entrega.EntregaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ConsumerPreparaEntrega {

    private final EntregaUseCase service;

    public ConsumerPreparaEntrega(final EntregaUseCase service) {
        this.service = service;
    }

    @Bean
    public Consumer<PreparaEntregaDTO> input() {
        return evento -> {
            this.service.prepara(evento);
            System.out.println("Evento consumido com sucesso!");
        };
    }


}
